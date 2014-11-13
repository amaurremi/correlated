package ca.uwaterloo.dataflow.correlated.collector

import com.ibm.wala.classLoader.{IMethod, CallSiteReference}
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import scala.collection.JavaConversions._

sealed trait ReceiverI

/**
 * This object is necessary for the correlated calls analysis.
 * When there are no relevant polymorphic receivers, all transfer functions
 * will contain empty transformation maps and will be considered equal.
 * So we add a fake receiver that guarantees that the transfer functions can be unequal.
 */
case object FakeReceiver extends ReceiverI

case class Receiver private(valueNumber: Int, method: IMethod) extends ReceiverI

object Receiver {

  /**
   * Returns a receiver for a call site if the call site is polymorphic
   */
  def apply(cg: CallGraph, cgNode: CGNode, callSiteRef: CallSiteReference): Option[Set[Receiver]] =
    if (callSiteRef.isDispatch && cg.getNumberOfTargets(cgNode, callSiteRef) > 1) {
      val calls = cgNode.getIR.getCalls(callSiteRef).toSet
      Some(
        calls map {
          call =>
            Receiver(call.getReceiver, cgNode.getMethod)
        })
    } else None
}
