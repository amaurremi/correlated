package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.types.{TypeReference, MethodReference}

trait SecretStrings extends SecretDefinition {

  private[this] val superTypeNames = Set("Ljava/lang/String", "Ljava/lang/Object")

  override def isSecret(method: MethodReference) = method.getName.toString == "secret"

  override def isSecretSupertype(typeRef: TypeReference) = superTypeNames contains typeRef.getName.toString
}
