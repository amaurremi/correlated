package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.{VariableFacts, WalaInstructions}
import ca.uwaterloo.dataflow.correlated.collector.{FakeReceiver, ReceiverI, Receiver}
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.{IMethod, IClass}
import com.ibm.wala.ssa.{SSAReturnInstruction, SSAInvokeInstruction}

trait CorrelatedCallsProblem extends CorrelatedCallsProblemBuilder with WalaInstructions with VariableFacts { this: IfdsProblem =>

  override type FactElem = ValueNumber

  override def otherSuccEdges: IdeEdgeFn  =
    (ideN1, n2) =>
      ideN1.n.getLastInstruction match {
        case returnInstr: SSAReturnInstruction =>
          val d2s = ifdsOtherSuccEdges(ideN1, n2)
          // setting local variables to bottom
          val localReceivers = ccReceivers filter {
            case Receiver(vn, method) =>
              method == ideN1.n.getMethod &&   // considering only local variables
                returnInstr.getResult != vn && // not setting to bottom the return value
                (method.isStatic || vn != 1)   // excluding this
            case _                    =>
              false
          }
          val edgeFn = CorrelatedFunction((localReceivers map { _ -> composedTypesBottom }).toMap)
          d2s map { FactFunPair(_, edgeFn) }
        case _                                 =>
          ifdsOtherSuccEdges(ideN1, n2) flatMap idFactFunPairSet
      }

  override def endReturnEdges: IdeEdgeFn =
    (ideN1, n2) =>
      ifdsEndReturnEdges(ideN1, n2) flatMap idFactFunPairSet

  private[this] def getCcReceiver(vn: ValueNumber, method: IMethod): Option[ReceiverI] =
    ccReceivers find {
      case Receiver(v, m) =>
        v == vn && m == method
      case FakeReceiver   =>
        false
    }

  override def callReturnEdges: IdeEdgeFn =
    (ideN1, n2) => {
      val n1 = ideN1.n
      val d2s = ifdsCallReturnEdges(ideN1, n2)
      val edgeFn = n1.getLastInstruction match {
        case invokeInstr: SSAInvokeInstruction if !invokeInstr.isStatic =>
          getCcReceiver(invokeInstr.getReceiver, n1.getMethod) map {
            rec =>
              CorrelatedFunction(Map(
                rec -> composedTypesTop
            ))
          }
        case _                                                          =>
          None
      }
      edgeFn match {
        case Some(f) =>
          d2s map { FactFunPair(_, f) }
        case None    =>
          d2s flatMap idFactFunPairSet
      }
    }

  override def callStartEdges: IdeEdgeFn =
    (ideN1, n2) => {
      val d2s = ifdsCallStartEdges(ideN1, n2)
      val localsInTargetToBottomMap: ComposedTypeMultiMap = (ccReceivers collect {
        case r@Receiver(vn, method)
          if method == n2.getMethod && (method.isStatic || vn != 1) =>
            r -> composedTypesBottom
      }).toMap
      if (n2.getMethod.isStatic)
        d2s map { FactFunPair(_, CorrelatedFunction(localsInTargetToBottomMap)) }
      else {
        val n1 = ideN1.n
        val edgeFn = n1.getLastInstruction match {
          case invokeInstr: SSAInvokeInstruction =>
            getCcReceiver(invokeInstr.getReceiver, n1.getMethod) match {
              case Some(rec) =>
                val matchStaticTypes = Map(rec -> ComposedTypes(SetType(staticTypes(invokeInstr, n1, n2)), TypesTop))
                CorrelatedFunction(matchStaticTypes ++ localsInTargetToBottomMap)
              case None =>
                CorrelatedFunction(localsInTargetToBottomMap)
            }
        }
        d2s map {
          FactFunPair(_, edgeFn)
        }
      }
    }

  private[this] def staticTypes(callInstr: SSAInvokeInstruction, sourceNode: Node, targetNode: Node): Set[IClass] = {
    val enclosingClass = enclProc(targetNode).getMethod.getDeclaringClass
    getDeclaringClasses(callInstr, sourceNode)(enclosingClass) + enclosingClass
  }
}
