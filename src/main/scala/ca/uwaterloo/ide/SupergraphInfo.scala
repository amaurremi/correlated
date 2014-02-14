package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph

class SupergraphInfo[T, P](superGraph: ISupergraph[T, P]) {

  /**
   * Returns all edges with a given source node
   */
  def edgesWithSource(n: IdeNode[T]): Seq[IdeEdge[T]] = ???

  /**
   * Returns the edge function corresponding to an edge
   */
  def edgeFn(edge: IdeEdge[T]): IdeFunction = ???

  /**
   * Returns the enclosing procedure of a given node
   */
  def enclProc(n: T): T = ???

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  def startNode(node: IdeNode[T]): IdeNode[T] = ???

  /**
   * Let p be the node's enclosing procedure. This method returns all
   * edges from a p's caller node to the corresponding return node.
   */
  def callReturnEdges(node: IdeNode[T]): Seq[IdeEdge[T]] = {
    val proc = enclProc(node.n)
    ???
  }
}
