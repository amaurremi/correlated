package ca.uwaterloo.dataflow.ifds.instance.taint

import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.types.TypeReference

trait SecretDefinition {

  def isSecret(method: IMethod): Boolean

  def isSecretSupertype(typeRef: TypeReference): Boolean

  def isSecretOperation(operationName: String): Boolean
}
