package ca.uwaterloo.ide.analysis.taint

import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.{SSAReturnInstruction, SSAInvokeInstruction}
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
            case Variable(m, retVal) if retVal == returnInstr.getResult          => // we are returning a secret value
              (methodToReturnVars.get(m).asScala map {
                FactFunPair(_, Id)
              })(breakOut)
            case retVal if methodToReturnVars.get(n1.getMethod).contains(retVal) => // we are returning a non-secret value
              Set.empty
            case _                                                               =>
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
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction =>
          getParameterNumber(ideN1, callInstr) match { // checks if we are passing d1 as an argument to the function
            case Some(argNum)                                            =>
              val substituteFact = Variable(n2.getMethod, getValNumFromParameterNum(n2, argNum))
              Set(FactFunPair(substituteFact, Id))
            case None if d1 == Λ && callValNum(callInstr).isDefined =>
              val callVar = Variable(n1.getMethod, callValNum(callInstr).get)
              methodToReturnVars.put(n2.getMethod, callVar) // todo is that the right way to keep track of return variables?
              Set(FactFunPair(d1, Id), FactFunPair(callVar, Id))
            case None                                                    =>
              idFactFunPairSet(d1)
          }
        case _ => throw new UnsupportedOperationException("callStartEdges invoked on non-call instruction")
      }
    }

  private[this] val methodToReturnVars = new HashSetMultiMap[IMethod, Variable]

  private[this] def isSecret(method: IMethod) = method.getName.toString == "secret"
}
