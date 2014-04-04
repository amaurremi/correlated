package ca.uwaterloo.ide.analysis.taint

import ca.uwaterloo.ide.TraverseGraph
import ca.uwaterloo.ide.analysis.{WalaInstructions, VariableFacts}
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.CallGraph
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._

abstract class TaintAnalysisBuilder(fileName: String) extends WalaInstructions with VariableFacts { this: TraverseGraph =>

  private[this] val config =
    ConfigFactory.load(
      "ide/analysis/taint/" + fileName,
      ConfigParseOptions.defaults.setAllowMissing(false),
      ConfigResolveOptions.defaults
    )

  private[this] val builder              = FlexibleCallGraphBuilder()(config)
  private[this] val callGraph: CallGraph = builder.cg

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure

  override def getValNum(vn: ValueNumber, n: IdeNode): ValueNumber = vn

  override type LatticeElem = TaintLatticeElem
  override type IdeFunction = TaintFunction
  override type FactElem    = ValueNumber

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = TaintFunction
  override val λTop: IdeFunction   = TaintFunction

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
    override def toString: String = "bottom (secret)" // todo not sure
  }

  sealed trait TaintFunction extends IdeFunctionI

  case object TaintFunction extends TaintFunction {

    override def apply(elem: LatticeElem): LatticeElem = elem

    override def ◦(f: TaintFunction): TaintFunction = TaintFunction

    override def ⊓(f: TaintFunction): TaintFunction = TaintFunction
  }
}
