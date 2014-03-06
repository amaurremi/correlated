package ca.uwaterloo.ide.example.cp

import ca.uwaterloo.ide.IdeProblem
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._

abstract class ConstantPropagation(fileName: String) extends IdeProblem {

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
  override type Fact      = Int

  override val intToFact: Int => Fact = identity
  override val factToInt: Fact => Int = identity
  override val Λ: Fact                = 0

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure // todo not sure
}
