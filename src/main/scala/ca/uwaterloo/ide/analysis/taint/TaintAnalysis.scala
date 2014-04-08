package ca.uwaterloo.ide.analysis.taint

import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.{SSAReturnInstruction, SSAInvokeInstruction}
import com.ibm.wala.types.MethodReference
import com.ibm.wala.util.collections.HashSetMultiMap
import scala.collection.JavaConverters._
import scala.collection.breakOut

class TaintAnalysis(fileName: String) extends TaintAnalysisBuilder(fileName) with IdeProblem with IdeSolver {

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn =
    (ideN1, n2) => {
      val n1 = ideN1.n
      val d1 = ideN1.d
      n1.getLastInstruction match {
        case returnInstr: SSAReturnInstruction if hasRetValue(returnInstr) =>
          d1 match {
            // we are returning a secret value, because an existing (i.e. secret) fact d1 is returned
            case v@Variable(m, _) if isFactReturned(v, n1, returnInstr.getResult) =>
              (methodToReturnVars.get(m).asScala map {
                FactFunPair(_, Id)
              })(breakOut)
            case _                                                                =>
              idFactFunPairSet(d1)
          }
        case _                                                             =>
          idFactFunPairSet(d1)
      }
    }

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: EdgeFn =
    (ideN1, _) =>
      idFactFunPairSet(ideN1.d)

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: EdgeFn =
    (ideN1, _) =>
      idFactFunPairSet(ideN1.d) // todo not for fields/static variables

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartEdges: EdgeFn =
    (ideN1, n2) => {
      val n1 = ideN1.n
      val d1 = ideN1.d
      val targetMethod = n2.getMethod
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction if isSecret(targetMethod.getReference) =>
          if (d1 == Λ)
            idFactFunPairSet(d1) + FactFunPair(Variable(targetMethod, callInstr.getReturnValue(0)), Id)
          else
            idFactFunPairSet(d1)
        case callInstr: SSAInvokeInstruction                           =>
          getParameterNumber(ideN1, callInstr) match { // checks if we are passing d1 as an argument to the function
            case Some(argNum)                                       =>
              val substituteFact = Variable(targetMethod, getValNumFromParameterNum(n2, argNum))
              Set(FactFunPair(substituteFact, Id))
            case None if d1 == Λ && callValNum(callInstr).isDefined =>
              methodToReturnVars.put(targetMethod, Variable(n1.getMethod, callValNum(callInstr).get)) // todo is this the right way to keep track of return variables?
              idFactFunPairSet(d1)
            case None                                               =>
              idFactFunPairSet(d1)
          }
        case _                                                         =>
          throw new UnsupportedOperationException("callStartEdges invoked on non-call instruction")
      }
    }

  private[this] val methodToReturnVars = new HashSetMultiMap[IMethod, Variable]

  def isSecret(method: MethodReference) = method.getName.toString == "secret"

  private[this] def isFactReturned(d: Variable, n: Node, retVal: ValueNumber): Boolean =
    d.elem == retVal && d.method == n.getMethod
}
