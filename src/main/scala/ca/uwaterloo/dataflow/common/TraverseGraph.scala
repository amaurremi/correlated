package ca.uwaterloo.dataflow.common

import scala.collection.JavaConverters._

trait TraverseGraph { this: ExplodedGraphTypes =>

  def followingNodes(n: NodeOrPhi): Seq[NodeOrPhi] =
    n match {
      case PhiNode(node)    =>
        Seq(NormalNode(node))
      case NormalNode(node) =>
        (supergraph getSuccNodes node).asScala.toSeq map PhiNode // todo optimize (don't return phi node if there's no phi instruction)
    }

  /**
   * Returns the enclosing procedure of a given node.
   */
  lazy val enclProc: Node => Procedure = supergraph.getProcOf

  /**
   * Given a call node n, returns the start nodes of n's target procedures.
   */
  def targetStartNodes(n: NodeOrPhi): Iterator[NodeOrPhi] =
    (supergraph getCalledNodes n.node).asScala map PhiNode

  /**
   * Return-site nodes that correspond to call node n
   */
  def returnNodes(n: NodeOrPhi): Iterator[NodeOrPhi] =
    targetStartNodes(n) flatMap { s =>
      supergraph.getReturnSites(n.node, enclProc(s.node)).asScala map PhiNode // todo don't create phi if there's no phi instr
    }

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: Node=> Seq[NodeOrPhi] = { // todo: in general, not sure to which scala collections WALA's collections should be converted
    n =>
      val nodes = supergraph getEntriesForProcedure enclProc(n)
      nodes.view.toSeq map PhiNode // todo if no phi node, make it normal node
  }

  /**
   * Given the exit node of procedure p, returns all pairs (c, r), where c calls p with corresponding
   * return-site node r.
   */
  def callReturnPairs(exit: NodeOrPhi): Seq[(NormalNode, NodeOrPhi)] = { // todo is this correct?
    for {
      r <- followingNodes(exit)
      rn = r.node
      if supergraph isReturn rn
      c <- supergraph.getCallSites(rn, enclProc(exit.node)).asScala
    } yield NormalNode(c) -> r
  }
  
  /**
   * All intra-procedural nodes from the start of a procedure.
   */
  def allNodesInProc(node: NodeOrPhi): Seq[NodeOrPhi] =
    for {
      s <- startNodes(node.node)
      n <- nodesInProc(s, enclProc(node.node))
    } yield n

  private[this] def nodesInProc(
    startNode: NodeOrPhi,
    proc: Procedure,
    acc: Set[NodeOrPhi] = Set.empty
  ): Set[NodeOrPhi] =
    if (enclProc(startNode.node) != proc)
      acc
    else followingNodes(startNode).toSet flatMap {
      (next: NodeOrPhi) =>
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
