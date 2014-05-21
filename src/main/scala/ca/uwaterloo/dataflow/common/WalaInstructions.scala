package ca.uwaterloo.dataflow.common

import com.ibm.wala.classLoader.IClass
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.ssa.{SSAReturnInstruction, SSAInstruction, SSAInvokeInstruction}
import scala.collection.JavaConverters._
import scala.collection.breakOut

trait WalaInstructions { this: VariableFacts with TraverseGraph =>

  override type Node      = BasicBlockInContext[IExplodedBasicBlock]
  override type Procedure = CGNode

  def firstParameter(instr: SSAInvokeInstruction): Int =
    if (instr.isStatic) 0 else 1

  /**
   * If the variable corresponding to this node's fact is passed as a parameter to this call instruction,
   * returns the number of the parameter.
   */
  def getParameterNumber(node: XNode, callInstr: SSAInvokeInstruction): Option[Int] =
    node.d match {
      case Variable(method, elem)           =>
        val valNum = getValNum(elem, node)
        firstParameter(callInstr) to callInstr.getNumberOfParameters - 1 find {
          callInstr.getUse(_) == valNum
        }
      case ArrayElement | Field(_) | Lambda => None // todo fields???
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
      enclProc(node).getIR.iterateNormalInstructions.asScala

  /**
   * Get the value number for the ith parameter.
   * @param argNum the number of the parameter
   * @param n a node inside of the method
   */
  def getValNumFromParameterNum(n: Node, argNum: Int): ValueNumber =
    enclProc(n).getIR.getSymbolTable.getParameter(argNum)

  /**
   * Get the value number for the ith parameter.
   * @param argNum the number of the parameter, excluding this // todo account for non-static methods
   * @param instr the call instruction
   */
  def getValNumFromParameterNum(instr: SSAInvokeInstruction, argNum: Int): ValueNumber =
    instr.getUse(argNum)

  /**
   * The value number of a call's return value.
   */
  def callValNum(callInstr: SSAInvokeInstruction): Option[ValueNumber] =
    if (callInstr.getNumberOfReturnValues == 1)
      Some(callInstr.getReturnValue(0))
    else None

  def hasRetValue(retInstr: SSAReturnInstruction) = retInstr.getResult >= 0

  def getMethodName(node: Node): String = node.getMethod.getName.toString

  def getCalledNodes(node: Node) = (supergraph getCalledNodes node).asScala

  private[WalaInstructions] def getReceiverTypes(pa: PointerAnalysis, callInstr: SSAInvokeInstruction, node: CGNode): Set[IClass] =
    callValNum(callInstr) match {
      case Some(vn) =>
        val key = pa.getHeapModel.getPointerKeyForLocal(node, vn)
        (pa.getPointsToSet(key).asScala map {
          _.getConcreteType
        })(breakOut)
      case None =>
        Set.empty[IClass]
    }

  lazy val getDeclaringClasses: (PointerAnalysis, SSAInvokeInstruction, Node) => Map[IClass, Set[IClass]] =
    (pa, callInstr, sourceNode) => {
      val targetSelector = new ClassHierarchyMethodTargetSelector(sourceNode.getMethod.getClassHierarchy)
      getReceiverTypes(pa, callInstr, sourceNode.getNode).foldLeft(Map[IClass, Set[IClass]]() withDefaultValue Set.empty[IClass]) {
        case (result, receiverType) =>
          val targetMethod = targetSelector.getCalleeTarget(sourceNode.getNode, callInstr.getCallSite, receiverType)
          if (targetMethod == null)
            result
          else {
            val targetClass = targetMethod.getDeclaringClass
            result + (targetClass -> (result(targetClass) + receiverType))
          }
      }
    }
}
