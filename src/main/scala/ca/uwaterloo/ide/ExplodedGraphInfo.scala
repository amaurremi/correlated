package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph

class ExplodedGraphInfo[T, P, V <: IdeFunction[V]](
  supergraph: ISupergraph[T, P],
  edgeFn: EdgeFn[T, V]
) {

  import edgeFn.keys

  /**
   * All edges with a given source.
   */
  lazy val edgesWithSource: IdeNode[T] => Set[IdeEdge[T]] =
    source =>
      keys filter {
        _.source == source
      }

  lazy val ideNodes: T => Set[IdeNode[T]] =
    n =>
      keys collect {
        case edge if edge.source.n == n => edge.source
        case edge if edge.target.n == n => edge.target
      }

  /**
   * All edges with a given target node.
   */
  lazy val edgesWithTarget: IdeNode[T] => Set[IdeEdge[T]] =
    target =>
      keys filter {
        _.target == target
      }

  /**
   * Returns all edges from a given call node to a start node.
   */
  def callStartEdges(n: T): Set[IdeEdge[T]] =
    for {
      node                          <- ideNodes(n)
      e@IdeEdge(_, StartNode(_, _)) <- edgesWithSource(node)
    } yield e

  /**
   * Returns the enclosing procedure of a given node.
   */
  lazy val enclProc: T => P =
    n =>
      supergraph getProcOf n

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: T => Array[IdeNode[T]] =
    supergraph getEntriesForProcedure enclProc(_) flatMap ideNodes

  /**
   * Let p be the node's enclosing procedure. This method returns all
   * edges from a p's caller node to the corresponding return node.
   */
  def callReturnEdges(node: T): Seq[IdeEdge[T]] = {
    val proc = enclProc(node)
    ???
  }

  /**
   * All corresponding call-return edges in the exploded graph.
   */
  lazy val allCallReturnEdges: Seq[IdeEdge[T]] = ???
  
  /**
   * All intra-procedural edges from the start of a procedure.
   */
  lazy val intraEdgesFromStart: Seq[IdeEdge[T]] = ???
  
  /**
   * Returns all call nodes for a given procedure.
   */
  lazy val getCallNodes: P => Seq[IdeNode[T]] = ???

  /**
   * Returns an iterator over the exploded graph that corresponds to the supergraph.
   */
  lazy val explodedGraphIterator: Iterator[IdeNode[T]] = ???

  /**
   * All nodes that are not call nodes or start nodes.
   */
  lazy val notCallOrStartNodes: Seq[IdeNode[T]] = ???
}
