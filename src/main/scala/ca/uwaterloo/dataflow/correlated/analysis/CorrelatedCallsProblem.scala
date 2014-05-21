package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.{VariableFacts, WalaInstructions}
import ca.uwaterloo.dataflow.correlated.collector.{FakeReceiver, ReceiverI, Receiver}
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.{IMethod, IClass}
import com.ibm.wala.ssa.SSAInvokeInstruction
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis

trait CorrelatedCallsProblem extends CorrelatedCallsProblemBuilder with WalaInstructions with VariableFacts { this: IfdsProblem =>

  def pointerAnalysis: PointerAnalysis

  override type FactElem = ValueNumber

  override def otherSuccEdges: IdeEdgeFn  =
    (ideN1, n2) =>
      ifdsOtherSuccEdges(ideN1, n2) flatMap idFactFunPairSet

  override def endReturnEdges: IdeEdgeFn =
    (ideN1, n2) => {
      val d2s = ifdsEndReturnEdges(ideN1, n2)
      // setting local variables to bottom (all types)
      val optLocalVar = ideN1.d match {
        case Variable(method, elem)
          if method == ideN1.n.getMethod && (method.isStatic || elem != 1) => // todo excluding this. correct?
          // todo what if the receiver is a field?
            Some(elem)
        case _                                                             =>
          None
      }
      optLocalVar match {
        case Some(localVar) =>
          val receiver = getCcReceiver(localVar, ideN1.n.getMethod)
          val edgeFn = receiver match {
            case Some(rec) =>
              CorrelatedFunction(Map(rec -> composedTypesBottom))
            case None      =>
              Id
          }
          val localToBottom  = (d2s - Λ) map {
            FactFunPair(_, edgeFn)
          }
          val maybeLambdaSet = if (ideN1.d == Λ) idFactFunPairSet(Λ) else Set.empty
          maybeLambdaSet ++ localToBottom
        case None           => d2s flatMap idFactFunPairSet
      }
    }
  
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
      if (n2.getMethod.isStatic)
        d2s flatMap idFactFunPairSet
      else {
        val n1 = ideN1.n
        val edgeFn = n1.getLastInstruction match {
          case invokeInstr: SSAInvokeInstruction =>
            getCcReceiver(invokeInstr.getReceiver, n1.getMethod) match {
              case Some(rec) =>
                CorrelatedFunction(Map(rec -> ComposedTypes(SetType(staticTypes(invokeInstr, n1, n2)), TypesTop)))
              case None =>
                Id
            }
        }
        d2s map {
          FactFunPair(_, edgeFn)
        }
      }
    }

  private[this] def staticTypes(callInstr: SSAInvokeInstruction, sourceNode: Node, targetNode: Node): Set[IClass] = {
    val enclosingClass = enclProc(targetNode).getMethod.getDeclaringClass
    getDeclaringClasses(pointerAnalysis, callInstr, sourceNode)(enclosingClass) + enclosingClass
  }
}
