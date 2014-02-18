package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.PathEdge

case class IdeEdge[T](source: IdeNode[T], target: IdeNode[T]) {

  def getWalaPathEdge: PathEdge[T] = PathEdge.createPathEdge(source.n, source.d.n, target.n, target.d.n)
}

object IdeEdge {

  def apply[T](pathEdge: PathEdge[T]): IdeEdge[T] =
    IdeEdge(IdeNode(pathEdge.getEntry, Fact(pathEdge.getD1)), IdeNode(pathEdge.getTarget, Fact(pathEdge.getD2)))
}
