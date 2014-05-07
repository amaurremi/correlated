package ca.uwaterloo.dataflow.ide.cp

import ca.uwaterloo.dataflow.ide.PropagationSpecBuilder
import ca.uwaterloo.dataflow.ide.instance.cp.CopyConstantPropagation

class CopyConstantPropagationSpecBuilder(fileName: String) extends CopyConstantPropagation(fileName) with PropagationSpecBuilder {

  def isConstant(elem: LatticeElem): Boolean =
    elem match {
      case n: Num => true
      case _      => false
    }

  def shouldBeAConstant(variable: SpecVariableFact) {
    variable shouldSatisfy isConstant
  }

  def shouldNotBeAConstant(variable: SpecVariableFact) {
    variable shouldSatisfy { !isConstant(_) }
  }
}
