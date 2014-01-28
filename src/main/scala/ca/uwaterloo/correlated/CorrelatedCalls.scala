package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.CallGraphUtil
import ca.uwaterloo.correlated.util.Converter._
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.util.graph.traverse.DFS

/**
 * Data structure that contains information about the program with respect to correlated calls.
 */
case class CorrelatedCalls(

  /*
   * All call graph nodes
   */
  cgNodes: Set[CGNode] = Set.empty,

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
   * Total number of call graph nodes
   */
  lazy val cgNodeNum = cgNodes.size

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
      rcNum,                                              // 6
      rcNodeNum,                                          // 7
      rcCcReceiverNum                                     // 8
    )
}

object CorrelatedCalls {

  val empty = CorrelatedCalls()

  /**
   * Creates a CorrelatedCalls object for a given call graph.
   */
  def apply(cg: CallGraph): CorrelatedCalls = {
    val rcs = CallGraphUtil.getRcs(cg)
    val cgNodes  = toScalaList(DFS.getReachableNodes(cg).iterator)
    val ccWriter = CorrelatedCallsWriter(rcs, cgNodes)
    ccWriter.written
  }
}
