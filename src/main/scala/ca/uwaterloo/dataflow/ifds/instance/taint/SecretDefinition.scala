package ca.uwaterloo.dataflow.ifds.instance.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import com.ibm.wala.analysis.typeInference.TypeAbstraction
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.types.TypeReference

trait SecretDefinition extends VariableFacts {

  sealed trait SecretOperation
  case object ReturnsSecretValue extends SecretOperation
  case object ReturnsSecretArray extends SecretOperation
  case object ConcatenatesStrings extends SecretOperation
  case object StringConcatConstructor extends SecretOperation
  case object NonSecretLibraryCall extends SecretOperation
  case object SecretLibraryCall extends SecretOperation

  case class LibraryOptions(
    excludePrefixes: Set[String],
    defaultSecretTypes: Set[String],
    whiteList: Set[String]
  )

  def isSecret(method: IMethod): Boolean

  def secretType: String

  def isConcatClass(typeRef: TypeAbstraction): Boolean

  def isSecretArrayElementType(typeRef: TypeReference): Boolean

  def getOperationType(node: CGNode, vn: Option[ValueNumber]): Option[SecretOperation]
}
