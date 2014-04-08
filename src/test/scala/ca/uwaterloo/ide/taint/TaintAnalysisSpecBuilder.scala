package ca.uwaterloo.ide.taint

import ca.uwaterloo.ide.PropagationSpecBuilder
import ca.uwaterloo.ide.analysis.taint.TaintAnalysis
import com.ibm.wala.ssa.SSAInvokeInstruction

class TaintAnalysisSpecBuilder(fileName: String) extends TaintAnalysis(fileName) with PropagationSpecBuilder {

  override val Assignment: InstructionType = SecretAssignment

  private[this] case object SecretAssignment extends InstructionType {

    override def instrName = "secret assignment"

    override def doesMatch =
      node =>
        getInstruction(node) match {
          case n: SSAInvokeInstruction if isSecret(n.getDeclaredTarget) => true
          case _                                                        => false
        }
  }
}
