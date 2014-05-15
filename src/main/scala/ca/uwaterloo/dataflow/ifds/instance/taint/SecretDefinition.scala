package ca.uwaterloo.dataflow.ifds.instance.taint

import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.types.TypeReference

trait SecretDefinition {

  sealed trait SecretOperation
  case object ReturnsSecretString extends SecretOperation
  case object ReturnsNonSecretString extends SecretOperation
  case object ReturnsSecretArray extends SecretOperation
  case object ReturnsNonSecretArray extends SecretOperation

  def isSecret(method: IMethod): Boolean

  def isSecretType(typeRef: TypeReference): Boolean

  def getOperationType(op: String): Option[SecretOperation]

  def assumeSecretByDefault: Boolean
}
