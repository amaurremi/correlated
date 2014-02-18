package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph
import scala.collection.JavaConverters._

class SupergraphInfo[T, P, V](supergraph: ISupergraph[T, P]) {

  /**
   * Returns all edges with a given source node
   */
  def edgesWithSource(n: IdeNode[T]): Seq[IdeEdge[T]] = ???

  /**
   * Returns the edge function corresponding to an edge
   */
  def edgeFn(edge: IdeEdge[T]): V = ???

  /**
   * Returns the enclosing procedure of a given node
   */
  def enclProc(n: T): T = ???

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  def startNodes(node: T): Seq[IdeNode[T]] = ???

  /**
   * Let p be the node's enclosing procedure. This method returns all
   * edges from a p's caller node to the corresponding return node.
   */
  def callReturnEdges(node: T): Seq[IdeEdge[T]] = {
    val proc = enclProc(node)
    ???
  }

  def getCallnodes(proc: P): Seq[T] = ???
  
  def supergraphIterator = supergraph.iterator().asScala
}
