package ca.uwaterloo.dataflow.correlated.collector

import com.ibm.wala.classLoader.{IMethod, CallSiteReference}
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}

sealed trait ReceiverI

case object FakeReceiver extends ReceiverI

case class Receiver private(valueNumber: Int, method: IMethod) extends ReceiverI

object Receiver {

  /**
   * Returns a receiver for a call site if the call site is polymorphic
   */
  def apply(cg: CallGraph, cgNode: CGNode, callSiteRef: CallSiteReference): Option[Receiver] =
    if (callSiteRef.isDispatch && cg.getNumberOfTargets(cgNode, callSiteRef) > 1) {
      val calls = cgNode.getIR.getCalls(callSiteRef)
      /*if (calls.length > 1) {
        assert(
          (calls map { _.getReceiver }).distinct.size == 1,
          "Since getCalls refers to one call site, all its receivers should be the same."
        )
      }*/
      // todo: account for multiple receivers of a call site
      Some(Receiver(calls(0).getReceiver, cgNode.getMethod))
    } else None
}
