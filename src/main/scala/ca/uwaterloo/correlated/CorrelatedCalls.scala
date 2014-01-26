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
  cgNodeNum: Long = 0,
  /*
   * Recursive components of the graph. A recursive component is a strongly connected component
   * of the graph that consists of at least two nodes, or, if it consists of a single node, then
   * that node has a self-loop.
   */
  rcs: List[Set[CGNode]] = List.empty,
  /*
   * Receivers of correlated calls that are contained in a recursive component
   */
  rcCcReceivers: Set[Receiver] = Set.empty,
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
   * Amount of correlated call receivers
   */
  lazy val ccReceiverNum: Int = receiverToCallSites.size

  /**
   * Total number of call sites
   */
  lazy val totalCallSiteNum = totalCallSites.size

  /**
   * Number of multiple dispatch call sites
   */
  lazy val dispatchCallSiteNum = dispatchCallSites.size

  /**
   * Number of correlated call sites
   */
  lazy val ccSiteNum = ccSites.size

  /**
   * Number of recursive components
   * @see CorrelatedCalls.rcs
   */
  lazy val rcNum = rcs.size

  /**
   * Number of nodes in recursive components
   */
  lazy val rcNodeNum = rcs.flatten.size

  /**
   * Number of correlated call receivers in recursive components
   */
  lazy val rcCcReceiverNum = rcCcReceivers.size

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
      "%7d recursive components (RCs)\n" +                // 6
      "%7d nodes in RCs\n" +                              // 7
      "%7d CC receivers in nodes in RCs\n\n",             // 8
      cgNodeNum,                                          // 1
      totalCallSiteNum,                                   // 2
      dispatchCallSiteNum,                                // 3
      ccSiteNum,                                          // 4
      ccReceiverNum,                                      // 5
      rcNum,                                             // 6
      rcNodeNum,                                         // 7
      rcCcReceiverNum                                    // 8
    )
}

object CorrelatedCalls {

  val empty = CorrelatedCalls()

  /**
   * Creates a CorrelatedCalls object for a given call graph.
   */
  def apply(cg: CallGraph): CorrelatedCalls = {
    import Scalaz._

    val rcs = CallGraphUtil.getRcs(cg)
    val cgNodes  = toScalaList(DFS.getReachableNodes(cg).iterator)
    val ccWriter =
      for {
        _ <- CorrelatedCalls(rcs = rcs).tell
        _ <- cgNodes.traverse[CorrelatedCallWriter, CGNode](cgNodeWriter(rcs.flatten.toSet))
      } yield ()
    ccWriter.written
  }

  private[this] def cgNodeWriter(
    rcs: Set[CGNode]
  )(
    cgNode: CGNode
  ): CorrelatedCallWriter[CGNode] = {
    import Scalaz._

    val recToCallSites = receiverToCallSites(cgNode)
    for {
      _ <- CorrelatedCalls(
        cgNodeNum             = 1,
        totalCallSites      = callSiteIterator(cgNode).toSet,
        receiverToCallSites = recToCallSites,
        rcCcReceivers        = if (rcs contains cgNode) recToCallSites.keys.toSet else Set.empty
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
