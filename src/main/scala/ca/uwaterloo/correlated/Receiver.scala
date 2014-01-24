package ca.uwaterloo.correlated

import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.types.MethodReference

case class Receiver private(valueNumber: Int, methodRef: MethodReference)

object Receiver {

  def apply(cgNode: CGNode, callSiteRef: CallSiteReference): Option[Receiver] =
    if (callSiteRef.isDispatch) {
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