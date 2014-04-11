package ca.uwaterloo.id.ide.analysis

import ca.uwaterloo.id.ide.TraverseGraph
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.ssa.{SSAReturnInstruction, SSAInstruction, SSAInvokeInstruction}
import scala.collection.JavaConverters._

trait WalaInstructions { this: VariableFacts with TraverseGraph =>

  override type Node      = BasicBlockInContext[IExplodedBasicBlock]
  override type Procedure = CGNode

  /**
   * If the variable corresponding to this node's fact is passed as a parameter to this call instruction,
   * returns the number of the parameter.
   */
  def getParameterNumber(node: XNode, callInstr: SSAInvokeInstruction): Option[Int] =
    node.d match {
      case Variable(method, elem) =>
        val valNum = getValNum(elem, node)
        0 to callInstr.getNumberOfParameters - 1 find { // todo starting with 0 because we're assuming it's a static method
          callInstr.getUse(_) == valNum
        }
      case Lambda                 => None
    }

  /**
   * Get all instructions following instruction in node `n` in its procedure.
   */
  def followingInstructions(n: Node): Iterator[SSAInstruction] =
    followingNodes(n) map { _.getLastInstruction }

  /**
   * Does the value with the given value number correspond to a method call?
   */
 def isCall(value: ValueNumber, node: Node): Boolean =
    instructionsInProc(node) exists {
      case instr: SSAInvokeInstruction
        if instr.getReturnValue(0) == value => true
      case _                                => false
    }

  /**
   * Get all instructions in the procedure enclosing this node.
   */
  lazy val instructionsInProc =
    (node: Node) =>
      enclProc(node).getIR.iterateNormalInstructions().asScala

  /**
   * Get the value number for the ith parameter.
   * @param argNum the number of the parameter
   * @param n a node inside of the method
   */
  def getValNumFromParameterNum(n: Node, argNum: Int): ValueNumber =
    enclProc(n).getIR.getSymbolTable.getParameter(argNum)

  /**
   * The value number of a call's return value.
   */
  def callValNum(callInstr: SSAInvokeInstruction): Option[ValueNumber] =
    if (callInstr.getNumberOfReturnValues == 1)
      Some(callInstr.getReturnValue(0))
    else None

  def hasRetValue(retInstr: SSAReturnInstruction) = retInstr.getResult >= 0
}
