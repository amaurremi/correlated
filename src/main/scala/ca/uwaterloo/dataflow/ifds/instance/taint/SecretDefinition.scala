package ca.uwaterloo.dataflow.ifds.instance.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.types.{MethodReference, TypeReference}

trait SecretDefinition extends VariableFacts {

  sealed trait SecretOperation
  case object ReturnsStaticSecretOrPreservesSecret extends SecretOperation
  case object ReturnsSecretArray extends SecretOperation
  case object ConcatenatesStrings extends SecretOperation
  case object StringConcatConstructor extends SecretOperation
  case object NonSecretLibraryCall extends SecretOperation
  case object SecretLibraryCall extends SecretOperation
  case object SecretIfSecretArgument extends SecretOperation

  case class LibraryOptions(
    excludePrefixes: Set[String],
    defaultSecretTypes: Set[String],
    whiteList: Set[MethodNameAndClass],
    secretIfArgument: Set[MethodNameAndClass]
  )

  protected case class MethodNameAndClass(
    methodName: String,
    klass: String
  )

  def isSecret(method: MethodReference): Boolean

  def secretTypes: Set[String]

  def isConcatClass(typeRef: TypeReference): Boolean

  def isSecretArrayElementType(typeRef: TypeReference): Boolean

  def getOperationType(op: MethodReference, node: CGNode, vn: Option[ValueNumber]): Option[SecretOperation]

  def mainArgsSecret: Boolean
}
