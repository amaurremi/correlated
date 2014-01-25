package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.CallGraphUtil
import ca.uwaterloo.correlated.util.Converter._
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.util.graph.traverse.DFS
import CorrelatedCallsWriter._
import scalaz.Scalaz

/**
 * Data structure that contains information about the program with respect to correlated calls.
 */
case class CorrelatedCalls(
  /*
   * Total amount of call graph nodes
   */
  cgNodes: Long = 0,
  /*
   * Recursive components of the graph. A recursive component is a strongly connected component
   * of the graph that consists of at least two nodes, or, if it consists of a single node, then
   * that node has a self-loop.
   */
  sccs: List[Set[CGNode]] = List.empty,
  /*
   * Receivers of correlated calls that are contained in a recursive component
   */
  sccCcReceivers: Set[Receiver] = Set.empty,
  /*
   * Maps a receiver to a set of call sites that are invoked on that receiver
   */
  receiverToCallSites: MultiMap[Receiver, CallSiteReference] = Map.empty,
  /*
   * Total amount of reachable call sites
   */
  totalCallSites: Set[CallSiteReference] = Set.empty
) {

  /**
   * All correlated call sites
   */
  lazy val ccSites: Set[CallSiteReference] =
    receiverToCallSites.values.flatten.toSet

  /**
   * Total amount of multiple dispatch call sites
   */
  lazy val dispatchCallSites: Set[CallSiteReference] =
    totalCallSites filter { _.isDispatch }

  /**
   * Prints out the information related to correlated calls.
   */
  def printInfo() =
    printf(
      "%7d call graph nodes\n" +                          // 1
      "%7d total call sites\n" +                          // 2
      "%7d dispatch call sites\n\n" +                     // 3
      "%7d correlated calls (CCs)\n" +                    // 4
      "%7d CC receivers\n\n" +                            // 5
      "%7d strongly connected components (SCCs)\n" +      // 6
      "%7d nodes in SCCs\n" +                             // 7
      "%7d CC receivers in nodes in SCCs\n\n",            // 8
      cgNodes,                                            // 1
      totalCallSites.size,                                // 2
      dispatchCallSites.size,                             // 3
      ccSites.size,                                       // 4
      receiverToCallSites.size,                           // 5
      sccs.size,                                          // 6
      sccs.flatten.size,                                  // 7
      sccCcReceivers.size                                   // 8
    )
}

object CorrelatedCalls {

  val empty = CorrelatedCalls()

  /**
   * Creates a CorrelatedCalls object for a given call graph.
   */
  def apply(cg: CallGraph): CorrelatedCalls = {
    import Scalaz._

    val sccs = CallGraphUtil.getSccs(cg)
    val cgNodes  = toScalaList(DFS.getReachableNodes(cg).iterator)
    val ccWriter =
      for {
        _ <- CorrelatedCalls(sccs = sccs).tell
        _ <- cgNodes.traverse[CorrelatedCallWriter, CGNode](cgNodeWriter(sccs.flatten.toSet))
      } yield ()
    ccWriter.written
  }

  private[this] def cgNodeWriter(
    sccs: Set[CGNode]
  )(
    cgNode: CGNode
  ): CorrelatedCallWriter[CGNode] = {
    import Scalaz._

    val recToCallSites = receiverToCallSites(cgNode)
    for {
      _ <- CorrelatedCalls(
        cgNodes             = 1,
        totalCallSites      = callSiteIterator(cgNode).toSet,
        receiverToCallSites = recToCallSites,
        sccCcReceivers        = if (sccs contains cgNode) recToCallSites.keys.toSet else Set.empty
      ).tell
    } yield cgNode
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
