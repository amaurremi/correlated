package ca.uwaterloo.dataflow.common

import scala.collection.JavaConverters._

trait TraverseGraph { this: ExplodedGraphTypes with Phis =>

  def followingNodes(n: NodeType): Seq[NodeType] =
    n match {
      case PhiNode(node)    =>
        Seq(NormalNode(node))
      case NormalNode(node) =>
        (supergraph getSuccNodes node).asScala.toSeq map createNodeType
    }
  
  def createNodeType(node: Node): NodeType =
    if (phiInstructions(node).isEmpty)
      NormalNode(node)
    else
      PhiNode(node)

  /**
   * Returns the enclosing procedure of a given node.
   */
  lazy val enclProc: Node => Procedure = supergraph.getProcOf

  /**
   * Given a call node n, returns the start nodes of n's target procedures.
   */
  def targetStartNodes(n: NodeType): Iterator[NodeType] =
    (supergraph getCalledNodes n.node).asScala map createNodeType

  /**
   * Return-site nodes that correspond to call node n
   */
  def returnNodes(n: NodeType): Iterator[NodeType] =
    targetStartNodes(n) flatMap { s =>
      supergraph.getReturnSites(n.node, enclProc(s.node)).asScala map createNodeType
    }

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: Node=> Seq[NodeType] = { // todo: in general, not sure to which scala collections WALA's collections should be converted
    n =>
      val nodes = supergraph getEntriesForProcedure enclProc(n)
      nodes.view.toSeq map createNodeType
  }

  /**
   * Given the exit node of procedure p, returns all pairs (c, r), where c calls p with corresponding
   * return-site node r.
   */
  def callReturnPairs(exit: NodeType): Seq[(NormalNode, NodeType)] =
    if (!(supergraph isReturn exit.node)) // because for some reason that sometimes happens in WALA
      for {
        r <- followingNodes(exit)
        rn = r.node
        if supergraph isReturn rn
        c <- supergraph.getCallSites(rn, enclProc(exit.node)).asScala
      } yield NormalNode(c) -> r
    else Seq.empty[(NormalNode, NodeType)]

  /**
   * All intra-procedural nodes from the start of a procedure.
   */
  def allNodesInProc(node: NodeType): Seq[NodeType] =
    for {
      s <- startNodes(node.node)
      n <- nodesInProc(s, enclProc(node.node))
    } yield n

  private[this] def nodesInProc(
    startNode: NodeType,
    proc: Procedure,
    acc: Set[NodeType] = Set.empty
  ): Set[NodeType] =
    if (enclProc(startNode.node) != proc)
      acc
    else followingNodes(startNode).toSet flatMap {
      (next: NodeType) =>
        if (acc contains next)
          acc + startNode
        else
          nodesInProc(next, proc, acc + startNode)
    }

  /**
   * All call nodes inside of a given procedure
   */
  lazy val callNodesInProc: Procedure => Seq[NormalNode] =
    p =>
      for {
        s <- supergraph getEntriesForProcedure p
        n <- nodesInProc(NormalNode(s), p)
        node = n.node
        if supergraph isCall node
      } yield NormalNode(node)

  def traverseSupergraph = supergraph.iterator.asScala
}
