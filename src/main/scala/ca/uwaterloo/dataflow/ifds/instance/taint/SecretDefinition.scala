package ca.uwaterloo.dataflow.ifds.instance.taint

import com.ibm.wala.analysis.typeInference.TypeAbstraction
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.types.{MethodReference, TypeReference}

trait SecretDefinition {

  sealed trait SecretOperation
  case object ReturnsSecretValue extends SecretOperation
  case object ReturnsSecretArray extends SecretOperation
  case object ConcatenatesStrings extends SecretOperation
  case object StringConcatConstructor extends SecretOperation

  def isSecret(method: IMethod): Boolean

  def secretType: String

  def isConcatClass(typeRef: TypeAbstraction): Boolean

  def isSecretArrayElementType(typeRef: TypeReference): Boolean

  def getOperationType(op: MethodReference): Option[SecretOperation]
}
