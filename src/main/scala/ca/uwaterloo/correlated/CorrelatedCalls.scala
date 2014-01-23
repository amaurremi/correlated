package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.{MultiMap, Immutable}
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.types.MethodReference
import com.ibm.wala.util.graph.traverse.DFS.getReachableNodes
import scalaz.Scalaz._

case class Receiver(valueNumber: Int, methodRef: MethodReference)

case class CorrelatedCalls(
  receiverToCallSites: ImmutableMultiMap[Receiver, CallSiteReference],
  allCallSites: Long
)

object CorrelatedCalls {

  val empty: CorrelatedCalls = CorrelatedCalls(MultiMap.empty[Receiver, CallSiteReference], 0)

  private[this] def getReceiverToCallSites(cgNode: CGNode): ImmutableMultiMap[Receiver, CallSiteReference] = null  // todo

  private[this] def writeCallSites(cgNode: CGNode): CorrelatedCallWriter[CGNode] = {
    val callSiteNum = Immutable.convert(cgNode.iterateCallSites()).length
    for {
      _ <- CorrelatedCalls(getReceiverToCallSites(cgNode), callSiteNum).tell
    } yield cgNode
  }

  def apply(cg: CallGraph): CorrelatedCalls = {
    import CorrelatedCallsWriter._

    val nodes: java.util.Set[CGNode] = getReachableNodes(cg)
    val cgNodes = Immutable.convert(nodes.iterator).toList
    val correlatedCallWriter = cgNodes.traverse[CorrelatedCallWriter, CGNode](writeCallSites)
    correlatedCallWriter.written
  }
}
