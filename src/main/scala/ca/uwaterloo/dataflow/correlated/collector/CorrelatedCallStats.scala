package ca.uwaterloo.dataflow.correlated.collector

import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}

/**
 * Data structure that contains information about the program with respect to correlated calls
 */
final case class CorrelatedCallStats(

  /*
   * All call graph nodes
   */
  cgNodes: Set[CGNode] = Set.empty,

  /*
   * Recursive components of the graph. A recursive component is a strongly connected component
   * of the graph that consists of at least two nodes, or, if it consists of a single node, then
   * that node has a self-loop.
   */

  rcs: List[Set[CGNode]] = List.empty[Set[CGNode]],

  /*
   * Receivers of correlated calls that are contained in a recursive component
   */
  rcCcReceivers: Set[Receiver] = Set.empty[Receiver],

  /*
   * Maps a receiver to a set of call sites that are invoked on that receiver
   */
  receiverToCallSites: ReceiverToCallSites = Map.empty withDefaultValue Set.empty,

  /*
   * All call sites that are reachable in the call graph
   */
  totalCallSites: Set[CallSite] = Set.empty[CallSite],

  /**
   * Call sites that have more than one target
   */
  polymorphicCallSites: Set[CallSite] = Set.empty[CallSite]
) {

  /**
   * Total number of call graph nodes
   */
  def cgNodeNum = cgNodes.size

  /**
   * All correlated call sites
   */
  lazy val ccSites: Set[CallSite] =
    receiverToCallSites.values.flatten.toSet

  /**
   * Total amount of dispatch call sites
   */
  lazy val dispatchCallSites: Set[CallSite] =
    totalCallSites filter { _._1.isDispatch }

  def polymorphicCallSiteNum: Int =
    polymorphicCallSites.size

  /**
   * Amount of correlated call receivers
   */
  def ccReceiverNum: Int = receiverToCallSites.size

  /**
   * Total number of call sites
   */
  def totalCallSiteNum = totalCallSites.size

  /**
   * Number of dispatch call sites
   */
  def dispatchCallSiteNum = dispatchCallSites.size

  /**
   * Number of correlated call sites
   */
  def ccSiteNum = ccSites.size

  /**
   * Number of recursive components
   * @see CorrelatedCalls.rcs
   */
  def rcNum = rcs.size

  /**
   * Number of nodes in recursive components
   */
  def rcNodeNum = rcs.flatten.size

  /**
   * Number of correlated call receivers in recursive components
   */
  def rcCcReceiverNum = rcCcReceivers.size

  /**
   * Prints out the information related to correlated calls.
   */
  def printInfo() =
    printf(
      "%7d call graph nodes\n" +                          // 1
      "%7d total call sites\n" +                          // 2
      "%7d dispatch call sites\n" +                       // 3
      "%7d polymorphic call sites\n\n" +                  // 4
      "%7d correlated calls (CCs)\n" +                    // 5
      "%7d CC receivers\n\n" +                            // 6
      "%7d recursive components (RCs)\n" +                // 7
      "%7d nodes in RCs\n" +                              // 8
      "%7d CC receivers in nodes in RCs\n\n",             // 9
      cgNodeNum,                                          // 1
      totalCallSiteNum,                                   // 2
      dispatchCallSiteNum,                                // 3
      polymorphicCallSiteNum,                             // 4
      ccSiteNum,                                          // 5
      ccReceiverNum,                                      // 6
      rcNum,                                              // 7
      rcNodeNum,                                          // 8
      rcCcReceiverNum                                     // 9
    )

  def printCommaSeparated() {
    println(List(
      totalCallSiteNum,
      dispatchCallSiteNum,
      polymorphicCallSiteNum,
      ccSiteNum, ccReceiverNum,
      rcCcReceiverNum
    ).mkString(","))
  }
}

object CorrelatedCallStats {

  val empty = CorrelatedCallStats()

  /**
   * Creates a CorrelatedCalls object for a given call graph.
   */
  def apply(cg: CallGraph): CorrelatedCallStats =
    CorrelatedCallsWriter(cg).written
}
