package ca.uwaterloo.dataflow.common

import scala.collection.JavaConverters._

trait TraverseGraph { this: ExplodedGraphTypes =>

  def followingNodes(n: Node): Iterator[Node] =
    (supergraph getSuccNodes n).asScala

  /**
   * Returns the enclosing procedure of a given node.
   */
  lazy val enclProc: Node => Procedure = supergraph.getProcOf

  /**
   * Given a call node n, returns the start nodes of n's target procedures.
   */
  def targetStartNodes(n: Node): Iterator[Node] =
    (supergraph getCalledNodes n).asScala

  /**
   * Return-site nodes that correspond to call node n
   */
  def returnNodes(n: Node): Iterator[Node] =
    targetStartNodes(n) flatMap { s =>
      supergraph.getReturnSites(n, enclProc(s)).asScala
    }

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: Node => Seq[Node] = { // todo: in general, not sure to which scala collections WALA's collections should be converted
    n =>
      val nodes = supergraph getEntriesForProcedure enclProc(n)
      nodes.view.toSeq
  }

  /**
   * Given the exit node of procedure p, returns all pairs (c, r), where c calls p with corresponding
   * return-site node r.
   */
  def callReturnPairs(exit: Node): Iterator[(Node, Node)] = { // todo is this correct?
    for {
      r <- followingNodes(exit)
      if supergraph isReturn r
      c <- supergraph.getCallSites(r, enclProc(exit)).asScala
    } yield c -> r
  }

  /**
   * All intra-procedural nodes from the start of a procedure.
   */
  def allNodesInProc(node: Node): Seq[Node] =
    for {
      s <- startNodes(node)
      n <- nodesInProc(s, enclProc(node))
    } yield n

  private[this] def nodesInProc(
    startNode: Node,
    proc: Procedure,
    acc: Set[Node] = Set.empty
  ): Set[Node] =
    if (enclProc(startNode) != proc)
      acc
    else followingNodes(startNode).toSet flatMap {
      (next: Node) =>
        if (acc contains next)
          acc + startNode
        else
          nodesInProc(next, proc, acc + startNode)
    }

  /**
   * All call nodes inside of a given procedure
   */
  lazy val callNodesInProc: Procedure => Seq[Node] =
    p =>
      for {
        s <- supergraph getEntriesForProcedure p
        n <- nodesInProc(s, p)
        if supergraph isCall n
      } yield n

  def traverseSupergraph = supergraph.iterator.asScala
}
