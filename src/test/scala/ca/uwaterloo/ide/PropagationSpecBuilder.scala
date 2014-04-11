package ca.uwaterloo.id.ide

import ca.uwaterloo.id.ide.analysis.VariableFacts
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.SSAReturnInstruction
import org.scalatest.Assertions

trait PropagationSpecBuilder extends Assertions with VariableFacts { this: IdeProblem with IdeSolver =>

  /**
   * A variable with the given name that occurs in the given method.
   * Note that each variable within a method, and each method within the test program, should have a unique name.
   */
  def variable(name: String, method: String): SpecVariableFact = {
    val iMethod = getMethod(method)
    val variablesInMethod = solvedResult.keySet collect {
      case node@IdeNode(n, v: Variable) if v.method == iMethod && n.getMethod == iMethod && n.getLastInstruction != null =>
        (v, n)
    }
    val variableNodeProduct: Set[(Variable, Node)] =
      for {
        (v, n) <- variablesInMethod
        node   <- allNodesInProc(n)
        if node.getLastInstruction != null
      } yield (v, node)
    variableNodeProduct collectFirst {
      case (v@Variable(_, elem), n) if containedInLocalNames(name, n, getValNum(elem, IdeNode(n, Λ))) =>
        SpecVariable(v)
    } getOrElse NoVariable
  }

  private[this] def getMethod(name: String): IMethod =
    (solvedResult.keySet collectFirst {
      case IdeNode(n, _) if n.getMethod.getName.toString == name =>
        n.getMethod
    }).get

  private[this] def containedInLocalNames(name: String, n: Node, valNum: ValueNumber): Boolean =
    getLocalNames(n, valNum) contains name

  private[this] def getLocalNames(n: Node, valNum: ValueNumber): Seq[String] = {
    val locNames = enclProc(n).getIR.getLocalNames(n.getLastInstructionIndex, valNum)
    if (locNames == null) Seq.empty else locNames
  }

  trait SpecVariableFact {
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
    
    private[this] def mainReturnsAtFact: IdeNode = {
      (entryPoints flatMap allNodesInProc collectFirst {
        case node if node.getLastInstruction.isInstanceOf[SSAReturnInstruction] =>
          IdeNode(node, variable)
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
}
