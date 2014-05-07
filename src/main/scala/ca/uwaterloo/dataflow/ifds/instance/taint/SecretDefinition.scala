package ca.uwaterloo.dataflow.ifds.instance.taint

import com.ibm.wala.types.{TypeReference, MethodReference}

trait SecretDefinition {

  private[this] val superTypeNames = Set("Ljava/lang/String", "Ljava/lang/Object")

  final def isSecret(method: MethodReference) = method.getName.toString == "secret"

  final def isSecretSupertype(typeRef: TypeReference) = superTypeNames contains typeRef.getName.toString
}
