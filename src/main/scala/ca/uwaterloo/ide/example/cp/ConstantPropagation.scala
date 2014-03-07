package ca.uwaterloo.ide.example.cp

import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._
import com.ibm.wala.ssa.SSAInstruction

abstract class ConstantPropagation(fileName: String) extends IdeProblem with IdeSolver {

  private[this] val config =
    ConfigFactory.load(
      fileName,
      ConfigParseOptions.defaults().setAllowMissing(false),
      ConfigResolveOptions.defaults()
    )
  private[this] val builder              = FlexibleCallGraphBuilder()(config)
  private[this] val callGraph: CallGraph = builder.cg

  override type Node      = BasicBlockInContext[IExplodedBasicBlock]
  override type Procedure = CGNode
  override type Fact      = CpFact

  override val Λ: Fact    = CpFact(None)

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure // todo not sure

  /**
   * @param instruction Nothing represents the Λ fact, Some(...) represents instructions that ocrrespond to variable assignments.
   */
  case class CpFact(instruction: Option[SSAInstruction])
}
