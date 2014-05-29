package ca.uwaterloo.dataflow.ide.taint

import ca.uwaterloo.dataflow.common.{Method, AbstractIdeToIfds, VariableFacts}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsToIfds
import ca.uwaterloo.dataflow.ifds.conversion.{IdeToIfds, IdeFromIfdsBuilder}
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis
import ca.uwaterloo.dataflow.ifds.instance.taint.impl.{CcReceivers, SecretStrings}
import com.ibm.wala.classLoader.IField
import com.ibm.wala.ssa.{DefUse, SSAFieldAccessInstruction, SSAArrayLoadInstruction, SSAInvokeInstruction}
import org.scalatest.Assertions

sealed abstract class AbstractTaintAnalysisSpecBuilder (
  fileName: String
) extends IfdsTaintAnalysis(fileName) with VariableFacts with AbstractIdeToIfds with Assertions {

  def secretAssertion(name: String) = Method(name, 1, isStatic = true, "V")

  protected val secret                    = secretAssertion("secret")
  protected val notSecret                 = secretAssertion("notSecret")
  protected val secretStandardNotSecretCc = secretAssertion("secretStandardNotSecretCc")

  /**
   * A map from method names to lattice elements. For a given assertion method, indicates what
   * lattice element should be expected.
   */
  val assertionMap: Map[Method, Boolean]

  def assertSecretValues() {
    traverseSupergraph collect {
      case node if (supergraph isCall node) && node.getLastInstruction.isInstanceOf[SSAInvokeInstruction] =>
        (node, node.getLastInstruction.asInstanceOf[SSAInvokeInstruction])
    } foreach {
      case (node, invokeInstr) =>
        targetStartNodes(NormalNode(node)) foreach {
          startNode =>
            assertionMap.get(Method(startNode.node.getMethod)) foreach {
              assertResult(_)(getResultAtCallNode(node, invokeInstr))
            }
        }
    }
  }

  def isSecretArrayElement(node: Node, vn: ValueNumber): Boolean =
    new DefUse(enclProc(node).getIR).getDef(vn).isInstanceOf[SSAArrayLoadInstruction] &&
      isSecretArrayElementType(getTypeInference(enclProc(node)).getType(vn).getTypeReference)

  def isSecretField(node: Node, vn: ValueNumber, field: IField): Boolean =
    new DefUse(enclProc(node).getIR).getDef(vn) match {
      case fieldInstr: SSAFieldAccessInstruction =>
        getIField(node.getMethod.getClassHierarchy, fieldInstr.getDeclaredField) == field
      case _                                     =>
        false
    }

  /**
   * Tells whether the argument of a secret-assertion method is secret.
   */
  private[this] def getResultAtCallNode(node: Node, instr: SSAInvokeInstruction): Boolean =
    ifdsResult.get(node) match {
      case Some(facts) if instr.getNumberOfParameters > 0 =>
        val num = getValNumFromParameterNum(instr, 0)
        facts exists {
          case Variable(method, elem) =>
            method == node.getMethod && elem == num
          case ArrayElement           =>
            isSecretArrayElement(node, num)
          case Field(f)               =>
            isSecretField(node, num, f)
          case Lambda                 =>
            false
        }
      case _ => false
    }
}

sealed abstract class AbstractTaintAnalysisStringSpecBuilder (
  fileName: String
) extends AbstractTaintAnalysisSpecBuilder(fileName) with SecretStrings

class TaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisStringSpecBuilder(fileName) with IdeFromIfdsBuilder with IdeToIfds {

  override val assertionMap: Map[Method, Boolean] =
    Map(secret -> true, notSecret -> false, secretStandardNotSecretCc -> true)
}

class CcTaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisStringSpecBuilder(fileName) with CorrelatedCallsToIfds with CcReceivers {

  override val assertionMap: Map[Method, Boolean] =
    Map(secret -> true, notSecret -> false, secretStandardNotSecretCc -> false)
}
