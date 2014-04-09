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

  def shouldBeAConstant(variable: SpecVariable) {
    variable shouldSatisfy isConstant
  }

  def shouldNotBeAConstant(variable: SpecVariable) {
    variable shouldSatisfy { !isConstant(_) }
  }
}
