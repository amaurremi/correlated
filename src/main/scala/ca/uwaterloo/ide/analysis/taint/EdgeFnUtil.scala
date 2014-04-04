package ca.uwaterloo.ide.analysis.taint

import com.ibm.wala.ssa.SSAInvokeInstruction

trait EdgeFnUtil {

  protected def isSecret(invokeInstr: SSAInvokeInstruction): Boolean = {
    val secret = invokeInstr.getDeclaredTarget.getName.toString == "secret"
    val hasReturnVal = invokeInstr.getNumberOfReturnValues == 1
    assert(!secret || hasReturnVal, "secret function should always return a value")
    secret && hasReturnVal
  }
}
