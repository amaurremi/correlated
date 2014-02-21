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
   * Let p be the node's enclosing procedure. This method returns all
   * edges from p's caller nodes to their corresponding return nodes.
   */
  def callReturnEdges(node: T): Seq[IdeEdge[T]] = { // todo not sure that's the right implementation
  val proc    = enclProc(node)
    val procEntryNodes = supergraph getEntriesForProcedure proc flatMap ideNodes
    val callNodes = procEntryNodes flatMap edgesWithTarget collect {
      case IdeEdge(CallNode(c, _), _) => c
    }
    val returnNodes = callNodes map {
      c =>
        supergraph.getReturnSites(c, proc)
    }
    for {
      n <- callNodes
      c <- ideNodes(n)
      e <- edgesWithSource(c)
      if returnNodes contains e.target
    } yield e
  }

  /**
   * Returns the enclosing procedure of a given node.
   */
  lazy val enclProc: T => P =
    n =>
      supergraph getProcOf n

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: T => Array[IdeNode[T]] = // todo: in general, not sure to which scala collections WALA's collections should be converted
    supergraph getEntriesForProcedure enclProc(_) flatMap ideNodes

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
  lazy val notCallOrStartNodes: Iterator[IdeNode[T]] =
    explodedGraphIterator filter {
      case CallNode(_, _) | StartNode(_, _) => false
      case _                                => true
    }
}
