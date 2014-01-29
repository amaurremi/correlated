package ca.uwaterloo.correlated

import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.types.MethodReference

case class Receiver private(valueNumber: Int, methodRef: MethodReference)

object Receiver {

  /**
   * Returns a receiver for a call site if the call site is polymorphic
   */
  def apply(cg: CallGraph, cgNode: CGNode, callSiteRef: CallSiteReference): Option[Receiver] = // todo: assert that if the call site is polymorphic, it's a dispatch call site
    if (callSiteRef.isDispatch /*&& cg.getNumberOfTargets(cgNode, callSiteRef) > 1*/) {
      val calls = cgNode.getIR.getCalls(callSiteRef)
      /*if (calls.length > 1) {
        assert(
          (calls map { _.getReceiver }).distinct.size == 1,
          "Since getCalls refers to one call site, all its receivers should be the same."
        )
      }*/
      // todo: account for multiple receivers of a call site
      Some(Receiver(calls(0).getReceiver, cgNode.getMethod.getReference))
    } else None
}