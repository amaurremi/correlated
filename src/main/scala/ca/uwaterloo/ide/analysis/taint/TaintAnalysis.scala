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

class TaintAnalysis(fileName: String) extends IdeProblem with IdeSolver {

  private[this] val config =
    ConfigFactory.load(
      fileName,
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
  override type IdeFunction = IdTaintFunction
  
  override val Λ: Fact    = Lambda

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = IdTaintFunction
  override val λTop: IdeFunction   = IdTaintFunction

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn = ???

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: EdgeFn = ???

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: EdgeFn = ???

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartEdges: EdgeFn = ???

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

  sealed trait IdTaintFunction

  case object IdTaintFunction extends IdeFunctionI {

    override def apply(elem: LatticeElem): LatticeElem = elem

    override def ◦(f: IdTaintFunction): IdTaintFunction = IdTaintFunction

    override def ⊓(f: IdTaintFunction): IdTaintFunction = IdTaintFunction
  }
}
