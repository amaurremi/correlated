package ca.uwaterloo.dataflow.ifds.instance.taint

import com.ibm.wala.types.{TypeReference, MethodReference}

trait SecretDefinition {

  def isSecret(method: MethodReference): Boolean

  def isSecretSupertype(typeRef: TypeReference): Boolean

  def isSecretOperation(operationName: String): Boolean
}
