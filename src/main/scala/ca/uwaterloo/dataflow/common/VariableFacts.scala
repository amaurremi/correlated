package ca.uwaterloo.dataflow.common

import com.ibm.wala.classLoader.IMethod

trait VariableFacts extends ExplodedGraphTypes with TraverseGraph with WalaInstructions {

  type FactElem
  type ValueNumber = Int

  override type Fact   = VariableFact
  override val Λ: Fact = Lambda

  /**
   * Represents a fact for the set D
   */
  abstract sealed class VariableFact

  /**
   * @param elem The element that corresponds to the left-hand-side variable in an assignment
   */
  case class Variable(method: IMethod, elem: FactElem) extends VariableFact {
    override def toString: String = elem.toString + " in " + method.getName.toString + "()"
  }

  /**
   * Represents the Λ fact
   */
  case object Lambda extends VariableFact {
    override def toString: String = "Λ"
  }

  /**
   * Get value number from fact element
   */
  def getValNum(factElem: FactElem, node: XNode): ValueNumber
}
