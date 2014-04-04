package ca.uwaterloo.ide.analysis.taint

import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._
import com.ibm.wala.ssa.SSAInvokeInstruction

class TaintAnalysis(fileName: String) extends IdeProblem with IdeSolver with EdgeFnUtil {

  private[this] val config =
    ConfigFactory.load(
      "ide/analysis/taint/" + fileName,
      ConfigParseOptions.defaults.setAllowMissing(false),
      ConfigResolveOptions.defaults
    )

  private[this] val builder              = FlexibleCallGraphBuilder()(config)
  private[this] val callGraph: CallGraph = builder.cg

  private[this] type ValueNumber = Long
  
  override type Node        = BasicBlockInContext[IExplodedBasicBlock]
  override type Procedure   = CGNode
  override type Fact        = TaintFact
  override type LatticeElem = TaintLatticeElem
  override type IdeFunction = TaintFunction
  
  override val Λ: Fact    = Lambda

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = TaintFunction
  override val λTop: IdeFunction   = TaintFunction

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn =
    (ideN1, n2) => {
      ideN1.n.getLastInstruction match {
        case invokeInstr: SSAInvokeInstruction =>
          if (isSecret(invokeInstr.getDeclaredTarget)) {
            val x = allNodesInProc(ideN1.n)
            val valNum = invokeInstr.getDef
            Set(FactFunPair(ideN1.d, Id))
          } else {
            Set(FactFunPair(ideN1.d, Id))
          }
        case _                                 =>
          Set(FactFunPair(ideN1.d, Id))
      }
    }

  private[this] val idf = (ideN1: IdeNode, n2: Node) => Set(FactFunPair(ideN1.d, Id))

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: EdgeFn = idf

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: EdgeFn = idf

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartEdges: EdgeFn = idf

  /**
   * Represents a fact for the set D
   */
  abstract sealed class TaintFact

  /**
   * A variable on the left-hand side of an assignment.
   * @param method The surrounding method of the variable
   * @param valNum The value number of the variable
   */
  case class VariableFact(method: IMethod, valNum: ValueNumber) extends TaintFact {
    override def toString: String = "variable " + valNum + " in " + method.getName.toString + "()"
  }

  /**
   * Represents the Λ fact
   */
  case object Lambda extends TaintFact {
    override def toString: String = "Λ"
  }

  /**
   * Represents lattice elements for the set L
   */
  sealed trait TaintLatticeElem extends Lattice {
    def ⊓(n: TaintLatticeElem): TaintLatticeElem
  }

  case object ⊤ extends TaintLatticeElem {
    override def ⊓(n: TaintLatticeElem) = n
    override def toString: String = "top (not secret)"
  }

  case object ⊥ extends TaintLatticeElem {
    override def ⊓(n: TaintLatticeElem) = ⊥
    override def toString: String = "bottom (secret)"
  }

  sealed trait TaintFunction extends IdeFunctionI

  case object TaintFunction extends TaintFunction {

    override def apply(elem: LatticeElem): LatticeElem = elem

    override def ◦(f: TaintFunction): TaintFunction = TaintFunction

    override def ⊓(f: TaintFunction): TaintFunction = TaintFunction
  }
}
