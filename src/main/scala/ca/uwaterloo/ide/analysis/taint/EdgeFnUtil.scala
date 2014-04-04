package ca.uwaterloo.ide.analysis.taint

import com.ibm.wala.types.MethodReference

trait EdgeFnUtil {

  protected def isSecret(method: MethodReference): Boolean =
    method.getName.toString == "secret"
}
