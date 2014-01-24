package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.Converter
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.util.graph.traverse.DFS.getReachableNodes
import scalaz.Scalaz._

case class CorrelatedCalls(
  receiverToCallSites: MultiMap[Receiver, CallSiteReference],
  allCallSites: Long
)

object CorrelatedCalls {

  val empty: CorrelatedCalls = CorrelatedCalls(Map.empty[Receiver, Set[CallSiteReference]], 0)

  private[this] def receiverToCallSites(
    cgNode: CGNode
  ): MultiMap[Receiver, CallSiteReference] = {
    val startMap = Map[Receiver, Set[CallSiteReference]]() withDefaultValue Set.empty
    Converter.convert(cgNode.iterateCallSites()).foldLeft(startMap) {
      (map, callSite) =>
         Receiver(cgNode, callSite) match {
           case Some(receiver) =>
             map.updated(receiver, map(receiver) + callSite)
           case None =>
             map
         }
    }
  }

  private[this] def writeCallSites(cgNode: CGNode): CorrelatedCallWriter[CGNode] = {
    val callSiteNum = Converter.convert(cgNode.iterateCallSites()).length
    for {
      _ <- CorrelatedCalls(receiverToCallSites(cgNode), callSiteNum).tell
    } yield cgNode
  }

  def apply(cg: CallGraph): CorrelatedCalls = {
    import CorrelatedCallsWriter._

    val nodes: java.util.Set[CGNode] = getReachableNodes(cg)
    val cgNodes = Converter.convert(nodes.iterator).toList // todo: Make toSeq?
    val correlatedCallWriter = cgNodes.traverse[CorrelatedCallWriter, CGNode](writeCallSites)
    correlatedCallWriter.written
  }
}
