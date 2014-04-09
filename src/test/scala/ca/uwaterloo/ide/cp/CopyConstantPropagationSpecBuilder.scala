package ca.uwaterloo.ide.cp

import ca.uwaterloo.ide.PropagationSpecBuilder
import ca.uwaterloo.ide.analysis.cp.CopyConstantPropagation
import com.ibm.wala.ssa.SSAArrayStoreInstruction

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
