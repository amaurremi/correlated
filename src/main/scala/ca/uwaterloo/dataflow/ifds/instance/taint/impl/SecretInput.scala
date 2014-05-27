package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.analysis.typeInference.TypeAbstraction
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.types.{MethodReference, TypeReference}

trait SecretInput extends SecretDefinition {

  override def getOperationType(op: MethodReference, node: CGNode, vn: Option[ValueNumber]): Option[SecretOperation] = ???

  override def isSecretArrayElementType(typeRef: TypeReference): Boolean = ???

  override def isConcatClass(typeRef: TypeAbstraction): Boolean = ???

  override def secretType: String = ???

  override def isSecret(method: IMethod): Boolean = ???
}
