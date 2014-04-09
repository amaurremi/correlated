package ca.uwaterloo.ide

import ca.uwaterloo.ide.analysis.VariableFacts
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.SSAReturnInstruction
import org.scalatest.Assertions

trait PropagationSpecBuilder extends Assertions with VariableFacts { this: IdeProblem with IdeSolver =>

  /**
   * A variable with the given name that occurs in the given method.
   * Note that each variable within a method, and each method within the test program, should have a unique name.
   */
  def variable(name: String, method: String): SpecVariable = {
    val iMethod = getMethod(method)
    val variablesInMethod = solvedResult.keySet collect {
      case node@IdeNode(n, v: Variable) if n.getMethod == iMethod && n.getLastInstruction != null =>
        (v, node)
    }
    val variable = (variablesInMethod collectFirst {
      case (v, node) if containedInLocalNames(name, node.n, getValNum(v.elem, node)) => v
    }).get
    SpecVariable(variable)
  }

  private[this] def getMethod(name: String): IMethod =
    (solvedResult.keySet collectFirst {
      case IdeNode(n, _) if n.getMethod.getName.toString == name =>
        n.getMethod
    }).get

  private[this] def containedInLocalNames(name: String, n: Node, valNum: ValueNumber): Boolean = {
    getLocalNames(n, valNum) contains name
  }

  private[this] def getLocalNames(n: Node, valNum: ValueNumber): Seq[String] = {
    val locNames = enclProc(n).getIR.getLocalNames(n.getLastInstructionIndex, valNum)
    if (locNames == null) Seq.empty else locNames
  }
  
  case class SpecVariable(variable: Variable) {
    
    private[this] def mainReturnsAtFact: IdeNode = {
      (entryPoints flatMap allNodesInProc collectFirst {
        case node if node.getLastInstruction.isInstanceOf[SSAReturnInstruction] =>
          IdeNode(node, variable)
      }).get
    }

    def shouldBe(expectedElem: LatticeElem) {
      val resultElem = solvedResult(mainReturnsAtFact)
      assertResult(expectedElem)(resultElem)
    }

    def shouldSatisfy(condition: LatticeElem => Boolean) {
      val resultElem = solvedResult(mainReturnsAtFact)
      assert(condition(resultElem))
    }
  }
}
