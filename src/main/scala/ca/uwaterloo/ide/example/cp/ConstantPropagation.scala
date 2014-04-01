package ca.uwaterloo.ide.example.cp

import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._

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

  override val Λ: Fact    = Lambda

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure // todo not sure

  val ideNodeString: IdeNode => String =
    node => {
      val instr = node.n.getLastInstruction
      "IdeNode(\n  n: " + (if (instr == null) "null" else instr.toString) +
        "\n  d: " + node.d.toString +
        "\n  method: " + (if (instr == null) "null" else node.n.getMethod.getName.toString) +
        ")"
    }

  /**
   * Represents a fact for the set D
   */
  abstract sealed class CpFact

  /**
   * @param elem The array element that corresponds to the left-hand-side variable in an assignment
   */
  case class SomeFact(method: IMethod, elem: FactElem) extends CpFact {
    override def toString: String = elem.toString + " in " + method.getName.toString + "()"
  }

  abstract class FactElem

  /**
   * If we pass an array element as a parameter, we will loose the information about the array reference and index, so we only store the value number
   */
  case class ArrayElemByValNumber(valNum: Int) extends FactElem

  /**
   * @param array The value number of the array element's array
   * @param index The value number of the array element's index
   */
  case class ArrayElemByArrayAndIndex(array: Int, index: Int) extends FactElem

  /**
   * Represents the Λ fact
   */
  case object Lambda extends CpFact {
    override def toString: String = "Λ"
  }

  def printResult(withNullInstructions: Boolean = false) {
    solvedResult collect {
      case (n, e) if withNullInstructions || n.n.getLastInstruction != null =>
        println(ideNodeString(n))
        println("-> ")
        println(e.toString)
        println()
    }
  }
}
