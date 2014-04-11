package ca.uwaterloo.id.ide.cp

import ca.uwaterloo.id.ide.PropagationSpecBuilder
import ca.uwaterloo.id.ide.analysis.cp.CopyConstantPropagation

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
