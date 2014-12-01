package ca.uwaterloo.dataflow.common

import com.ibm.wala.util.collections.Filter
import com.ibm.wala.util.graph.traverse.DFS
import scala.collection.JavaConversions._
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
  lazy val startNodes: Node => Seq[NodeType] = { // todo: in general, not sure to which scala collections WALA's collections should be converted
    n =>
      val nodes = supergraph getEntriesForProcedure enclProc(n)
      nodes.view.toSeq map createNodeType
  }

  /**
   * Given the exit node of procedure p, returns all pairs (c, r), where c calls p with corresponding
   * return-site node r.
   */
  def callReturnPairs(exit: NodeType): Seq[(NormalNode, NodeType)] =
    for {
      r <- followingNodes(exit)
      rn = r.node
      c <- getCallSites(rn, enclProc(exit.node))
      if (supergraph getSuccNodes c).asScala contains rn
    } yield NormalNode(c) -> r

  lazy val getCallSites: (Node, Procedure) => Iterator[Node] =
    (node, proc) =>
      supergraph.getCallSites(node, proc).asScala

  /**
   * All call nodes inside of a given procedure
   */
  lazy val callNodesInProc: Procedure => Seq[NormalNode] =
    p => {
      val nodesInProc = DFS.getReachableNodes(
        supergraph,
        (supergraph getEntriesForProcedure p).toSeq,
        new Filter[Node]() {
          override def accepts(n: Node): Boolean = enclProc(n) == p
        }
      ).toSeq
      nodesInProc collect {
        case nip if supergraph isCall nip =>
          NormalNode(nip)
      }
    }

  def traverseSupergraph = supergraph.iterator.asScala
}
