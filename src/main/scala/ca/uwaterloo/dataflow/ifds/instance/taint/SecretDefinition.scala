package ca.uwaterloo.dataflow.ifds.instance.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import com.ibm.wala.analysis.typeInference.TypeAbstraction
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.types.{MethodReference, TypeReference}

trait SecretDefinition extends VariableFacts {

  sealed trait SecretOperation
  case object PreservesSecretValue extends SecretOperation
  case object ReturnsSecretArray extends SecretOperation
  case object ConcatenatesStrings extends SecretOperation
  case object StringConcatConstructor extends SecretOperation
  case object NonSecretLibraryCall extends SecretOperation
  case object SecretLibraryCall extends SecretOperation

  case class LibraryOptions(
    excludePrefixes: Set[String],
    defaultSecretTypes: Set[String],
    whiteList: Set[MethodNameAndClass]
  )

  protected case class MethodNameAndClass(
    methodName: String,
    klass: String
  )

  def isSecret(method: IMethod): Boolean

  def secretTypes: Set[String]

  def isConcatClass(typeRef: TypeAbstraction): Boolean

  def isSecretArrayElementType(typeRef: TypeReference): Boolean

  def getOperationType(op: MethodReference, node: CGNode, vn: Option[ValueNumber]): Option[SecretOperation]
}
