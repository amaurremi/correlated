package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph
import scala.collection.JavaConverters._

class GraphInfo[T, P, V <: IdeFunction[V]](
  supergraph: ISupergraph[T, P]
) {

  /**
   * All edges with a given target node.
   */
  def edgesWithTarget(n: IdeNode[T]): Seq[IdeEdge[T]] = ???

  /**
   * Returns all edges from a given call node to a start node.
   */
  def callStartEdges(n: T): Seq[IdeEdge[T]] = ???

  /**
   * Returns the enclosing procedure of a given node.
   */
  lazy val enclProc: T => P = ???

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: T => Seq[IdeNode[T]] = ???

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
