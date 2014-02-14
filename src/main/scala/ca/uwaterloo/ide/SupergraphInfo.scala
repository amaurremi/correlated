package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph

class SupergraphInfo[T, P](superGraph: ISupergraph[T, P]) {

  def edgesWithSource(n: IdeNode[T]): Seq[IdeEdge[T]] = ???

  def edgeFn(edge: IdeEdge[T]): IdeFunction = ???

  /**
   * Returns n's enclosing procedure
   */
  def enclProc(n: T): T = ???

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  def startNode(node: IdeNode[T]): IdeNode[T] = ???

  /**
   *
   */
  def callReturnEdges(node: IdeNode[T]): Seq[IdeEdge[T]] = {
    val proc = enclProc(node.n)
    ???
  }
}
