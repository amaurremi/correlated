package ca.uwaterloo.dataflow.ifds.instance.taint

import com.ibm.wala.types.{TypeReference, MethodReference}

trait SecretDefinition {

  private[this] val superTypeNames = Set("Ljava/lang/String", "Ljava/lang/Object")

  def isSecret(method: MethodReference) = method.getName.toString == "secret"

  def isSecretSupertype(typeRef: TypeReference) = superTypeNames contains typeRef.getName.toString
}
