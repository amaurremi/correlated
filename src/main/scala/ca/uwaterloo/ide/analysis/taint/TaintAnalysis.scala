package ca.uwaterloo.ide.analysis.taint

import ca.uwaterloo.ide.analysis.{WalaInstructions, VariableFacts}
import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ssa.SSAInvokeInstruction
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._

class TaintAnalysis(fileName: String) extends IdeProblem with IdeSolver with EdgeFnUtil with VariableFacts with WalaInstructions {

  private[this] val config =
    ConfigFactory.load(
      "taint/" + fileName,
      ConfigParseOptions.defaults.setAllowMissing(false),
      ConfigResolveOptions.defaults
    )

  private[this] val builder              = FlexibleCallGraphBuilder()(config)
  private[this] val callGraph: CallGraph = builder.cg

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure

  override type LatticeElem = TaintLatticeElem
  override type IdeFunction = TaintFunction
  override type FactElem    = ValueNumber

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = TaintFunction
  override val λTop: IdeFunction   = TaintFunction

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn =
    (ideN1, n2) => {
      val idFactFunPair: Set[FactFunPair] = Set(FactFunPair(ideN1.d, Id))
      ideN1.n.getLastInstruction match {
        case invokeInstr: SSAInvokeInstruction
          if ideN1.d == Λ && isSecret(invokeInstr) =>
            idFactFunPair + FactFunPair(Variable(ideN1.n.getMethod, invokeInstr.getReturnValue(0)), Id)
        case _                                     =>
            idFactFunPair
      }
    }

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
  override def callStartEdges: EdgeFn =
    (ideN1, n2) =>
      ideN1.n.getLastInstruction match {
        case callInstr: SSAInvokeInstruction =>
          getParameterNumber(ideN1, callInstr) match {
            case Some(argNum) => // checks if we are passing d1 as an argument to the function
              val targetFact = ???
              Set(FactFunPair(targetFact, Id))
            case None         =>
              Set(FactFunPair(ideN1.d, Id))
          }
        case _ => throw new UnsupportedOperationException("callStartEdges invoked on non-call instruction")
      }

  override def getValNum(vn: ValueNumber, n: IdeNode): ValueNumber = vn

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
