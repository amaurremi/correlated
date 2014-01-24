package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.Converter.toScalaIterator
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.util.graph.traverse.DFS.getReachableNodes
import scalaz.Scalaz

case class CorrelatedCalls(
  receiverToCallSites: MultiMap[Receiver, CallSiteReference],
  totalCallSites: Long,
  dispatchCallSites: Long
) {
  val ccSites: Iterable[CallSiteReference] = {
    receiverToCallSites.values.flatten
  }

  val getInfo: String =
    "Correlated call (CC) receivers: " + receiverToCallSites.size +
    "\nCCs: " + ccSites.size +
    "\nTotal call sites: " + totalCallSites +
    "\nDispatch call sites: " + dispatchCallSites
}

object CorrelatedCalls {

  val empty = CorrelatedCalls(Map.empty, 0, 0)

  def apply(cg: CallGraph): CorrelatedCalls = {
    import CorrelatedCallsWriter._
    import Scalaz._

    val cgNodes  = toScalaIterator(getReachableNodes(cg).iterator).toList
    val ccWriter = cgNodes.traverse[CorrelatedCallWriter, CGNode](writeCallSites)
    ccWriter.written
  }

  private[this] def writeCallSites(cgNode: CGNode): CorrelatedCallWriter[CGNode] = {
    import Scalaz.ToWriterOps

    val callSites = callSiteIterator(cgNode).toSeq
    for {
      _ <- CorrelatedCalls(
        receiverToCallSites = receiverToCallSites(cgNode), // todo export as separate writer
        totalCallSites      = callSites.length,
        dispatchCallSites   = callSites count { _.isDispatch }
      ).tell
    } yield cgNode
  }

  private[this] def receiverToCallSites(
    cgNode: CGNode
  ): MultiMap[Receiver, CallSiteReference] = {
    val startMap = Map[Receiver, Set[CallSiteReference]]() withDefaultValue Set.empty
    callSiteIterator(cgNode).foldLeft(startMap) {
      (map, callSite) =>
        Receiver(cgNode, callSite) match {
          case Some(receiver) =>
            map.updated(receiver, map(receiver) + callSite)
          case _ =>
            map
        }
    }
  }

  private[this] def callSiteIterator(cgNode: CGNode): Iterator[CallSiteReference] = {
    toScalaIterator(cgNode.iterateCallSites())
  }
}
