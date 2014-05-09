package ca.uwaterloo.dataflow.ide.taint

import ca.uwaterloo.dataflow.common.{AbstractIdeToIfds, VariableFacts}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsToIfds
import ca.uwaterloo.dataflow.ifds.conversion.{IdeToIfds, IdeFromIfdsBuilder}
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis
import com.ibm.wala.ssa.{SSAFieldAccessInstruction, SSAArrayLoadInstruction, IR, SSAInvokeInstruction}
import com.ibm.wala.types.FieldReference
import org.scalatest.Assertions

sealed abstract class AbstractTaintAnalysisSpecBuilder (
  fileName: String
) extends IfdsTaintAnalysis(fileName) with VariableFacts with AbstractIdeToIfds with Assertions {

  protected val shouldBeSecret      = "shouldBeSecret"
  protected val shouldNotBeSecret   = "shouldNotBeSecret"
  protected val shouldNotBeSecretCc = "shouldNotBeSecretCc"

  /**
   * A map from method names to lattice elements. For a given assertion method, indicates what
   * lattice element should be expected.
   */
  val assertionMap: Map[String, Boolean]

  def assertSecretValues() {
    traverseSupergraph collect {
      case node if (supergraph isCall node) && node.getLastInstruction.isInstanceOf[SSAInvokeInstruction] =>
        (node, node.getLastInstruction.asInstanceOf[SSAInvokeInstruction])
    } foreach {
      case (node, invokeInstr) =>
        targetStartNodes(node) foreach {
          startNode =>
            assertionMap.get(getMethodName(startNode)) foreach {
              assertResult(_)(getResultAtCallNode(node, invokeInstr))
            }
        }
    }
  }

  def isValNumArrayElement(node: Node, valNum: ValueNumber): Boolean = // todo inefficient
    instructionsInProc(node) exists {
      case loadInstr: SSAArrayLoadInstruction =>
        loadInstr.getDef == valNum
      case _                                  =>
        false
    }

  def isValNumField(node: Node, valNum: ValueNumber, field: FieldReference): Boolean = // todo inefficient
    instructionsInProc(node) exists {
      case fieldInstr: SSAFieldAccessInstruction =>
        fieldInstr.getDef == valNum && fieldInstr.getDeclaredField == field
      case _                                     =>
        false
    }

  /**
   * Tells whether the argument of a secret assertion method like "shouldBeSecret" is secret.
   */
  private[this] def getResultAtCallNode(node: Node, instr: SSAInvokeInstruction): Boolean =
    ifdsResult.get(node) match {
      case Some(facts) if instr.getNumberOfParameters > 0 =>
        val num: ValueNumber = getValNumFromParameterNum(instr, 0)
        facts exists {
          case Variable(method, elem) =>
            method == node.getMethod && elem == num
          case ArrayElement           =>
            isValNumArrayElement(node, num)
          case Field(f)               =>
            isValNumField(node, num, f)
          case Lambda                 =>
            false
        }
      case _ => false
    }
}

class TaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisSpecBuilder(fileName) with IdeFromIfdsBuilder with IdeToIfds {

  override val assertionMap: Map[String, Boolean] =
    Map(shouldBeSecret -> true, shouldNotBeSecret -> false)
}

class CcTaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisSpecBuilder(fileName) with CorrelatedCallsToIfds {

  override val assertionMap: Map[String, Boolean] =
    Map(shouldBeSecret -> true, shouldNotBeSecretCc -> false)
}
