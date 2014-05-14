package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.types.{TypeReference, MethodReference}
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

trait SecretStrings extends SecretDefinition {

  private[this] val superTypeNames = Set("Ljava/lang/String", "Ljava/lang/Object")

  private[this] lazy val stringOperations: Set[String] =
    (ConfigFactory load "src/main/config/StringOperations" getStringList "stringOperations").asScala.toSet

  override def isSecret(method: MethodReference) = method.getName.toString == "secret"

  override def isSecretSupertype(typeRef: TypeReference) = superTypeNames contains typeRef.getName.toString

  override def isSecretOperation(operationName: String): Boolean =
    stringOperations contains operationName
  // todo split? toCharArray? subSequence? clone
}
