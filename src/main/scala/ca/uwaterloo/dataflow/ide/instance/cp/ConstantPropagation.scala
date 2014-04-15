package ca.uwaterloo.dataflow.ide.instance.cp

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ide.analysis.solver.IdeSolver
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ssa.{SSAArrayLoadInstruction, SSAArrayStoreInstruction}
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class ConstantPropagation(fileName: String) extends IdeProblem with IdeSolver with VariableFacts {

  private[this] val config =
    ConfigFactory.load(
      "ca/uwaterloo/dataflow/ide/cp/" + fileName,
      ConfigParseOptions.defaults().setAllowMissing(false),
      ConfigResolveOptions.defaults()
    )
  private[this] val builder              = FlexibleCallGraphBuilder()(config)
  private[this] val callGraph: CallGraph = builder.cg

  override type FactElem  = ArrayElem

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node]                   = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure

  val ideNodeString: XNode => String =
    node => {
      val instr = node.n.getLastInstruction
      "IdeNode(\n  n: " + (if (instr == null) "null" else instr.toString) +
        "\n  d: " + node.d.toString +
        "\n  method: " + (if (instr == null) "null" else node.n.getMethod.getName.toString) +
        ")"
    }

  override def getValNum(arrayElem: ArrayElem, node: XNode): ValueNumber =
    arrayElem match {
      case byRefInd: ArrayElemByArrayAndIndex =>
        updateAllArrayElementValNums(node.n)
        arrayElemsToValNums(byRefInd, node.n.getMethod)
      case ArrayElemByValNumber(vn)           =>
        vn
  }

  private[this] val valNumsToArrayElems = mutable.Map[(ValueNumber, IMethod), ArrayElemByArrayAndIndex]()
  private[this] val arrayElemsToValNums = mutable.Map[(ArrayElemByArrayAndIndex, IMethod), ValueNumber]()

  protected def getFactByValNum(fact: Fact, node: Node): Option[Variable] = {
    updateAllArrayElementValNums(node)
    val method = node.getMethod
    fact match {
      case Variable(method2, el: ArrayElemByArrayAndIndex) =>
        arrayElemsToValNums.get(el, method2) map {
          valNum =>
            Variable(method, ArrayElemByValNumber(valNum))
        }
      case sf@Variable(_, ArrayElemByValNumber(_)) =>
        Some(sf)
      case _ =>
        None
    }
  }

  private[this] def updateArrayMaps(arrayElem: ArrayElemByArrayAndIndex, valNum: ValueNumber, method: IMethod) {
    arrayElemsToValNums += (arrayElem, method) -> valNum
    valNumsToArrayElems += (valNum, method) -> arrayElem
  }

  private[this] def updateAllArrayElementValNums(node: Node) = // todo very inefficient
    allNodesInProc(node) foreach {
      n =>
        n.getLastInstruction match {
          case _: SSAArrayStoreInstruction    =>
            updateArrayElementValNums(n)
          case instr: SSAArrayLoadInstruction =>
            val method = node.getMethod
            val valNum = instr.getDef
            val elem   = ArrayElemByArrayAndIndex(instr.getArrayRef, instr.getIndex)
            updateArrayMaps(elem, valNum, method)
          case _                              => ()
        }
    }

  private[this] def updateArrayElementValNums(n: Node) = {
    val assignment = getAssignmentInstr(n)
    val arrayRef = assignment.getArrayRef
    val arrayInd = assignment.getIndex
    followingInstructions(n) collectFirst { // todo inefficient
      case instruction: SSAArrayLoadInstruction
        if instruction.getArrayRef == arrayRef && instruction.getIndex == arrayInd =>
        updateArrayMaps(ArrayElemByArrayAndIndex(arrayRef, arrayInd), instruction.getDef, n.getMethod)
    }
  }

  protected def getAssignmentInstr(node: Node): SSAArrayStoreInstruction =
    node.getLastInstruction match {
      case arrayAssignment: SSAArrayStoreInstruction =>
        arrayAssignment
      case _                                         =>
        throw new IllegalArgumentException("Currently, only array store assignments are handled")
    }

  abstract class ArrayElem

  /**
   * If we pass an array element as a parameter, we will loose the information about the array reference and index, so we only store the value number
   */
  case class ArrayElemByValNumber(valNum: ValueNumber) extends ArrayElem

  /**
   * @param array The value number of the array element's array
   * @param index The value number of the array element's index
   */
  case class ArrayElemByArrayAndIndex(array: Int, index: Int) extends ArrayElem

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
