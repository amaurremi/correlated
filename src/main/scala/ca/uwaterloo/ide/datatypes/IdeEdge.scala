package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.PathEdge

case class IdeEdge[T](source: IdeNode[T], target: IdeNode[T]) {

  def getWalaPathEdge: PathEdge[T] = new PathEdge[T](source.n, source.d, target.n, target.d)
}

object IdeEdge {

  def apply[T](pathEdge: PathEdge[T]): IdeEdge[T] =
    IdeEdge(IdeNode(pathEdge.getEntry, pathEdge.getD1), IdeNode(pathEdge.getTarget, pathEdge.getD2))
}
