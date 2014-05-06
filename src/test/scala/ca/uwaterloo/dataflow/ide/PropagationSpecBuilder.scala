package ca.uwaterloo.dataflow.ide

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ide.analysis.solver.IdeSolver
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.SSAInvokeInstruction
import org.scalatest.Assertions

trait PropagationSpecBuilder extends Assertions with VariableFacts with IdeProblem { this: IdeSolver =>

  /**
   * A variable with the given name that occurs in the given method.
   * Note that each variable within a method, and each method within the test program, should have a unique name.
   */
  private[this] def variable(name: String, method: String): SpecVariableFact = {
    val iMethod = getMethod(method)
    val variablesInMethod = solvedResult.keySet collect {
      case node@XNode(n, v: Variable) if v.method == iMethod && n.getMethod == iMethod && n.getLastInstruction != null =>
        (v, n)
    }
    val variableNodeProduct: Set[(Variable, Node)] =
      for {
        (v, n) <- variablesInMethod
        node   <- allNodesInProc(n)
        if node.getLastInstruction != null
      } yield (v, node)
    variableNodeProduct collectFirst {
      case (v@Variable(_, elem), n) if containedInLocalNames(name, n, getValNum(elem, XNode(n, Λ))) =>
        SpecVariable(v)
    } getOrElse NoVariable
  }

  private[this] def getMethod(name: String): IMethod =
    (solvedResult.keySet collectFirst {
      case XNode(n, _) if n.getMethod.getName.toString == name =>
        n.getMethod
    }).get

  private[this] def containedInLocalNames(name: String, n: Node, valNum: ValueNumber): Boolean =
    getLocalNames(n, valNum) contains name

  private[this] def getLocalNames(n: Node, valNum: ValueNumber): Seq[String] = {
    val locNames = enclProc(n).getIR.getLocalNames(n.getLastInstructionIndex, valNum)
    if (locNames == null) Seq.empty else locNames
  }

  sealed trait SpecVariableFact

  case object NoVariable extends SpecVariableFact

  case class SpecVariable(variable: VariableFact) extends SpecVariableFact

  def assertSecretValues(assertCCs: Boolean = false) {
    traverseSupergraph collect {
      case node if (supergraph isCall node) && node.getLastInstruction.isInstanceOf[SSAInvokeInstruction] =>
        (node, node.getLastInstruction.asInstanceOf[SSAInvokeInstruction])
    } foreach {
      case (node, invokeInstr) =>
        targetStartNodes(node) foreach {
          getMethodName(_) match {
            case "shouldBeSecret"                   =>
              assertResult(Bottom)(getResultAtCallNode(node, invokeInstr))
            case "shouldNotBeSecret"                =>
              assertResult(Top)(getResultAtCallNode(node, invokeInstr))
            case "shouldNotBeSecretCC" if assertCCs =>
              ???
            case _                                  =>
          }
        }
    }
  }

  private[this] def getResultAtCallNode(node: Node, instr: SSAInvokeInstruction): LatticeElem = {
    val optValue: Option[LatticeElem] = solvedResult collectFirst {
      case (s@XNode(`node`, Variable(method, elem)), value)
        if (method == node.getMethod) &&
           (instr.getNumberOfParameters > 0) &&
           (getValNum(elem, s) == getValNumFromParameterNum(instr, 0)) =>
        value
    }
    optValue getOrElse Top
  }
}
