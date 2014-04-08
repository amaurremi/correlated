package ca.uwaterloo.ide.cp

import ca.uwaterloo.ide.PropagationSpecBuilder
import ca.uwaterloo.ide.analysis.cp.CopyConstantPropagation
import com.ibm.wala.ssa.SSAArrayStoreInstruction

class CopyConstantPropagationSpecBuilder(fileName: String) extends CopyConstantPropagation(fileName) with PropagationSpecBuilder {

  override val Assignment: InstructionType = ArrayAssignment
  
  private[this] case object ArrayAssignment extends InstructionType {

    override def instrName = "assignment"

    override def doesMatch =
      node =>
        getInstruction(node) match {
          case n: SSAArrayStoreInstruction => true
          case _                           => false
        }
  }
}
