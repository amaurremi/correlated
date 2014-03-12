package ca.uwaterloo.ide.example.cp

import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._
import com.ibm.wala.types.MethodReference

abstract class ConstantPropagation(fileName: String) extends IdeProblem with IdeSolver {

  val ideNodeString: IdeNode => String =
    node => {
      val instr = node.n.getLastInstruction
      "IdeNode(\n  n: " + node.n.toString +
              "\n  d: " + node.d.toString +
              "\n  instruction: " + (if (instr == null) "null" else instr.toString) +
      ")"
    }

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

  override val Λ: Fact    = Lambda

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure // todo not sure

  /**
   * Represents a fact for the set D
   */
  abstract sealed class CpFact

  /**
   * @param v The value number that corresponds to the left-hand-side variable in an assignment
   */
  case class SomeFact(method: MethodReference, v: Int) extends CpFact

  /**
   * Represents the Λ fact
   */
  case object Lambda extends CpFact {
    override def toString: String = "Λ"
  }

  def printResult() {
    solvedResult map {
      case (n, e) =>
        println(ideNodeString(n))
        println("-> ")
        println(e.toString)
        println()
    }
  }
}
