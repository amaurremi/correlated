package ca.uwaterloo.dataflow.correlated.collector

import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CallGraph

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
  def apply(cg: CallGraph, callSite: CallSite): Option[Set[Receiver]] = {
    val CallSite(callSiteRef, cgNode) = callSite
    if (cg.getNumberOfTargets(cgNode, callSiteRef) > 1) {
      val calls = cgNode.getIR.getCalls(callSiteRef).toSet
      Some(
        calls map {
          call =>
            Receiver(call.getReceiver, cgNode.getMethod)
        })
    } /*else if (cg.getNumberOfTargets(cgNode, callSiteRef) < 1)
        throw new RuntimeException("call site corresponds to wrong CG node")*/
      else None
  }
}
