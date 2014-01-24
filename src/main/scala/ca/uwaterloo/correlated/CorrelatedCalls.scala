package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.Converter.toScalaIterator
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.util.graph.traverse.DFS.getReachableNodes
import CorrelatedCallsWriter._
import scalaz.Scalaz

case class CorrelatedCalls(
  cgNodes: Long = 0,
  receiverToCallSites: MultiMap[Receiver, CallSiteReference] = Map.empty,
  totalCallSites: Long = 0,
  dispatchCallSites: Long = 0
) {
  val ccSites: Iterable[CallSiteReference] = {
    receiverToCallSites.values.flatten
  }

  def printInfo() =
    printf(
      "%7d call graph nodes\n" +
        "%7d total call sites\n" +
        "%7d dispatch call sites\n" +
        "%7d CCs\n" +
        "%7d correlated call (CC) receivers\n",
      cgNodes, totalCallSites, dispatchCallSites, ccSites.size, receiverToCallSites.size
    )
}

object CorrelatedCalls {

  val empty = CorrelatedCalls()

  def apply(cg: CallGraph): CorrelatedCalls = {
    import Scalaz._

    val cgNodes  = toScalaIterator(getReachableNodes(cg).iterator).toList
    val ccWriter = cgNodes.traverse[CorrelatedCallWriter, CGNode](cgNodeWriter)
    ccWriter.written
  }

  private[this] def cgNodeWriter(cgNode: CGNode): CorrelatedCallWriter[CGNode] = {
    import Scalaz._

    val callSites = callSiteIterator(cgNode).toList // .toSeq   // todo remove
    for {
      _ <- callSites.traverse[CorrelatedCallWriter, CallSiteReference](callSiteWriter(cgNode))
      _ <- CorrelatedCalls(
        cgNodes             = 1,
        receiverToCallSites = receiverToCallSites(cgNode)
      ).tell
    } yield cgNode
  }

  private[this] def callSiteWriter(
    cgNode: CGNode
  )(
    callSiteRef: CallSiteReference
  ): CorrelatedCallWriter[CallSiteReference] = {
    import Scalaz._

    for {
      _ <- CorrelatedCalls(
        totalCallSites = 1,
        dispatchCallSites = if (callSiteRef.isDispatch) 1 else 0
      ).tell
    } yield callSiteRef
  }

  private[this] def receiverToCallSites(
    cgNode: CGNode
  ): MultiMap[Receiver, CallSiteReference] = {
    val startMap = Map[Receiver, Set[CallSiteReference]]() withDefaultValue Set.empty
    // Create a map from receivers to dispatch call sites
    val receiverToCallsiteMap = callSiteIterator(cgNode).foldLeft(startMap) {
      (map, callSite) =>
        Receiver(cgNode, callSite) match {
          case Some(receiver) =>
            map.updated(receiver, map(receiver) + callSite)
          case _ =>
            map
        }
    }
    // Return a map that only contains receivers with more than one corresponding call site
    receiverToCallsiteMap filter {
      case (_, set) => set.size > 1
    }
  }

  private[this] def callSiteIterator(cgNode: CGNode): Iterator[CallSiteReference] = {
    toScalaIterator(cgNode.iterateCallSites())
  }
}
