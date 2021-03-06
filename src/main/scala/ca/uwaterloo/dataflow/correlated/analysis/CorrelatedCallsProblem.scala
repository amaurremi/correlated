package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.{VariableFacts, WalaInstructions}
import ca.uwaterloo.dataflow.correlated.collector.{FakeReceiver, ReceiverI, Receiver}
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.{IMethod, IClass}
import com.ibm.wala.ssa.{SSAReturnInstruction, SSAInvokeInstruction}

trait CorrelatedCallsProblem extends CorrelatedCallsProblemBuilder with WalaInstructions with VariableFacts { this: IfdsProblem =>

  override type FactElem = ValueNumber

  override def otherSuccEdges(ideN1: XNode)  =
    ideN1.n.node.getLastInstruction match {
      case returnInstr: SSAReturnInstruction =>
        val d2s = ifdsOtherSuccEdges(ideN1)
        // setting local variables to bottom
        val localReceivers = ccReceivers filter {
          case Receiver(vn, method) =>
            method == ideN1.n.node.getMethod &&   // considering only local variables
              returnInstr.getResult != vn &&      // not setting to bottom the return value
              (method.isStatic || vn != 1)        // excluding this
          case _                    =>
            false
        }
        val edgeFn = CorrelatedFunction((localReceivers map { _ -> composedTypesBottom }).toMap)
        d2s map { FactFunPair(_, edgeFn) }
      case _                                 =>
        ifdsOtherSuccEdges(ideN1) flatMap idFactFunPairSet
    }

  override def endReturnEdges(ideN1: XNode, n2: NodeType) = {
    val d2s = ifdsEndReturnEdges(ideN1, n2)
    val n1: NodeType = ideN1.n
    val node1: Node = n1.node
    val exitingMethod: IMethod = node1.getMethod
    if (exitingMethod.isStatic)
      d2s flatMap idFactFunPairSet
    else {
      // on end-return edge, just as on call-start edge, map calling receiver to its static types
      val callInstructions = getCallInstructions(n1, n2).toSet
      val edgeFns = callInstructions collect {
        case invokeInstr: SSAInvokeInstruction =>
          getCcReceiver(invokeInstr.getReceiver, n2.node.getMethod) match {
            case Some(rec) =>
              val matchStaticTypes = Map(rec -> ComposedTypes(SetType(staticTypes(invokeInstr, getCallNodes(n1, n2), node1)), TypesTop))
              CorrelatedFunction(matchStaticTypes)
            case None =>
              Id
          }
      }
      // todo look into this!!!! there used to be an assertion that edgeFns.size == 1 but it's wrong!
      for {
        edgeFn <- edgeFns
        d2     <- d2s
      } yield FactFunPair(d2, edgeFn)
    }
  }

  private[this] def getCcReceiver(vn: ValueNumber, method: IMethod): Option[ReceiverI] =
    ccReceivers find {
      case Receiver(v, m) =>
        v == vn && m == method
      case FakeReceiver   =>
        false
    }

  override def callReturnEdges(ideN1: XNode, n2: NodeType) =
      ifdsCallReturnEdges(ideN1, n2) flatMap idFactFunPairSet

  override def callStartEdges(ideN1: XNode, n2: NodeType) = {
    val d2s = ifdsCallStartEdges(ideN1, n2)
    val localsInTargetToBottomMap: ComposedTypeMultiMap = (ccReceivers collect {
      case r@Receiver(vn, method)
        if method == n2.node.getMethod && (method.isStatic || vn != 1) =>
          r -> composedTypesBottom
    }).toMap
    if (n2.node.getMethod.isStatic)
      d2s map { FactFunPair(_, CorrelatedFunction(localsInTargetToBottomMap)) }
    else {
      val n1 = ideN1.n.node
      val edgeFn = n1.getLastInstruction match {
        case invokeInstr: SSAInvokeInstruction =>
          getCcReceiver(invokeInstr.getReceiver, n1.getMethod) match {
            case Some(rec) =>
              val matchStaticTypes = Map(rec -> ComposedTypes(SetType(staticTypes(invokeInstr, Seq(n1), n2.node)), TypesTop))
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


  override def otherSuccEdgesPhi(ideN: XNode) =
    ifdsOtherSuccEdgesPhi(ideN) flatMap idFactFunPairSet

  private[this] def staticTypes(callInstr: SSAInvokeInstruction, sourceNodes: Seq[Node], targetNode: Node): Set[IClass] = {
    val enclosingClass = enclProc(targetNode).getMethod.getDeclaringClass
    (sourceNodes.toSet flatMap {
      sn: Node =>
        getDeclaringClasses(callInstr, sn)(enclosingClass)
    }) + enclosingClass
  }
}
