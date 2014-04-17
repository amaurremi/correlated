package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.{VariableFacts, WalaInstructions}
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.ssa.SSAInvokeInstruction

trait CorrelatedCallsProblem extends CorrelatedCallsProblemBuilder with WalaInstructions with VariableFacts { this: IfdsProblem =>

  override def otherSuccEdges: IdeEdgeFn  = ???

  override def endReturnEdges: IdeEdgeFn  = ???

  override def callReturnEdges: IdeEdgeFn = ???

  override def callStartEdges: IdeEdgeFn  =
    (ideN1, n2) => {
      val d2s = ifdsCallStartEdges(ideN1, n2)
      val n1 = ideN1.n
      val edgeFns = n1.getLastInstruction match {
        case invokeInstr: SSAInvokeInstruction =>
          val receiver = Receiver(invokeInstr.getReceiver, enclProc(n1).getMethod)
          val types = staticTypes(n1)
      }
      ???
    }
}
