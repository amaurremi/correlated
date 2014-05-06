package ca.uwaterloo.dataflow.ide

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ide.analysis.solver.IdeSolver
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.{SSAInvokeInstruction, SSAReturnInstruction}
import org.scalatest.Assertions

trait PropagationSpecBuilder extends Assertions with VariableFacts with IdeProblem { this: IdeSolver =>

  /**
   * A map from method names to lattice elements. For a given assertion method, indicates what
   * lattice element should be expected.
   */
  val assertionMap: Map[String, LatticeElem]

  /**
   * A variable with the given name that occurs in the given method.
   * Note that each variable within a method, and each method within the test program, should have a unique name.
   */
  def variable(name: String, method: String): SpecVariableFact = {
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

  sealed trait SpecVariableFact {
    def shouldBe(elem: LatticeElem): Unit
    def shouldSatisfy(condition: LatticeElem => Boolean): Unit
  }

  case object NoVariable extends SpecVariableFact {

    override def shouldSatisfy(condition: (LatticeElem) => Boolean) {
      assert(condition(Top))
    }

    override def shouldBe(expectedElem: LatticeElem) {
      assertResult(expectedElem)(Top)
    }
  }

  case class SpecVariable(variable: VariableFact) extends SpecVariableFact {

    private[this] def mainReturnsAtFact: XNode = {
      (entryPoints flatMap allNodesInProc collectFirst {
        case node if node.getLastInstruction.isInstanceOf[SSAReturnInstruction] =>
          XNode(node, variable)
      }).get
    }

    override def shouldBe(expectedElem: LatticeElem) {
      val resultElem = solvedResult(mainReturnsAtFact)
      assertResult(expectedElem)(resultElem)
    }

    override def shouldSatisfy(condition: LatticeElem => Boolean) {
      val resultElem = solvedResult(mainReturnsAtFact)
      assert(condition(resultElem))
    }
  }

  def assertSecretValues() {
    traverseSupergraph collect {
      case node if (supergraph isCall node) && node.getLastInstruction.isInstanceOf[SSAInvokeInstruction] =>
        (node, node.getLastInstruction.asInstanceOf[SSAInvokeInstruction])
    } foreach {
      case (node, invokeInstr) =>
        targetStartNodes(node) foreach {
          startNode =>
            assertionMap.get(getMethodName(startNode)) foreach {
              assertResult(_)(getResultAtCallNode(node, invokeInstr))
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