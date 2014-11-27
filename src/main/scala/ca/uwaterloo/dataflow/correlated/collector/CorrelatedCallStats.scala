package ca.uwaterloo.dataflow.correlated.collector

import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}

/**
 * Data structure that contains information about the program with respect to correlated calls
 */
final case class CorrelatedCallStats(

  // All call graph nodes
  cgNodes: Set[CGNode] = Set.empty,

  // Recursive components of the graph. A recursive component is a strongly connected component
  // of the graph that consists of at least two nodes, or, if it consists of a single node, then
  // that node has a self-loop.
  rcs: List[Set[CGNode]] = List.empty[Set[CGNode]],

  // Receivers of correlated calls that are contained in a recursive component
  rcCcReceivers: Set[Receiver] = Set.empty[Receiver],

  // Maps a receiver to a set of call sites that are invoked on that receiver
  receiverToCallSites: ReceiverToCallSites = Map.empty withDefaultValue Set.empty,

  // All call sites that are reachable in the call graph
  totalCallSites: Set[CallSite] = Set.empty[CallSite],

  // Call sites that have more than one target
  polymorphicCallSites: Set[CallSite] = Set.empty[CallSite],

  // Call sites that have only one target
  monomorphicCallSites: Set[CallSite] = Set.empty[CallSite],

  // Static call sites
  staticCallSites: Set[CallSite] = Set.empty[CallSite]
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
    totalCallSites filter { _.csr.isDispatch }

  def polymorphicCallSiteNum: Int =
    polymorphicCallSites.size

  def monomorphicCallSiteNum: Int =
    monomorphicCallSites.size

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
   * Number of static call sites
   */
  def staticCallSiteNum = staticCallSites.size

  /**
   * Prints out the information related to correlated calls.
   */
  def printInfo() =
    println(
      s"$cgNodeNum call graph nodes\n" +
      s"$totalCallSiteNum total call sites\n" +
      s"$staticCallSiteNum static call sites\n" +
      s"$monomorphicCallSiteNum monomorphic call sites\n" +
      s"$dispatchCallSiteNum dispatch call sites\n" +
      s"$polymorphicCallSiteNum polymorphic call sites\n\n" +
      s"$ccSiteNum correlated calls (CC)\n" +
      s"$ccReceiverNum CC receivers\n\n" +
      s"$rcNum recursive components (RC)\n" +
      s"$rcNodeNum nodes in RC\n" +
      s"$rcCcReceiverNum CC receivers in nodes in RC\n\n")

  def printCommaSeparated() {
    println(List(
      totalCallSiteNum,
      dispatchCallSiteNum,
      staticCallSiteNum,
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
