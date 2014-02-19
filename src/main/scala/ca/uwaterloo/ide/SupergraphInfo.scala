package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph
import scala.collection.JavaConverters._

class SupergraphInfo[T, P, V](supergraph: ISupergraph[T, P]) {

  /**
   * Returns all edges with a given source node.
   */
  def edgesWithSource(n: IdeNode[T]): Seq[IdeEdge[T]] = ???

  /**
   * Returns all edges from a given call node to a start node
   */
  def callStartEdges(n: T): Seq[IdeEdge[T]] = ???

  /**
   * Returns the edge IDE function corresponding to an edge.
   */
  lazy val edgeFn: IdeEdge[T] => V = ???

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
   * Returns all call nodes for a given procedure.
   */
  lazy val getCallNodes: P => Seq[T] = ???

  /**
   * Returns a Scala Iterator over the supergraph's nodes.
   */
  lazy val supergraphIterator = supergraph.iterator().asScala

  lazy val notCallOrStartNodes: Seq[T] = ???

  /**
   * All corresponding call-return edges in the exploded graph
   */
  lazy val callReturnEdges: Seq[IdeEdge[T]] = ???
}
