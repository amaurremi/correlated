package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.Converter._
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.util.graph.traverse.DFS
import CorrelatedCallsWriter._
import scalaz.Scalaz
import ca.uwaterloo.correlated.util.CallGraphUtil

case class CorrelatedCalls(
  cgNodes: Long = 0,
  sccs: List[Set[CGNode]] = List.empty,
  receiverToCallSites: MultiMap[Receiver, CallSiteReference] = Map.empty,
  totalCallSites: Long = 0,
  dispatchCallSites: Long = 0
) {
  val ccSites: Iterable[CallSiteReference] = {
    receiverToCallSites.values.flatten
  }

  def printInfo() =
    printf(
      "%7d call graph nodes\n" +                          // 1
      "%7d total call sites\n" +                          // 2
      "%7d dispatch call sites\n\n" +                     // 3
      "%7d correlated calls (CCs)\n" +                    // 4
      "%7d CC receivers\n\n" +                            // 5
      "%7d strongly connected components (SCCs)\n" +      // 6
      "%7d nodes in SCCs",                                // 7
      cgNodes,                                            // 1
      totalCallSites,                                     // 2
      dispatchCallSites,                                  // 3
      ccSites.size,                                       // 4
      receiverToCallSites.size,                           // 5
      sccs.size,                                          // 6
      sccs.flatten.size                                   // 7
    )
}

object CorrelatedCalls {

  val empty = CorrelatedCalls()

  def apply(cg: CallGraph): CorrelatedCalls = {
    import Scalaz._

    val sccs =
      CallGraphUtil.getSccs(cg)
    val cgNodes             = toScalaList(DFS.getReachableNodes(cg).iterator)
    val ccWriter            =
      for {
        _ <- CorrelatedCalls(sccs = sccs).tell
        _ <- cgNodes.traverse[CorrelatedCallWriter, CGNode](cgNodeWriter(sccs))
      } yield ()
    ccWriter.written
  }

  private[this] def cgNodeWriter(
    sccs: List[Set[CGNode]]
  )(
    cgNode: CGNode
  ): CorrelatedCallWriter[CGNode] = {
    import Scalaz._

    val callSites = callSiteIterator(cgNode).toList
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
