package ca.uwaterloo.ide

final class EdgeFn[T, V <: IdeFunction[V]](
  edgeToFn: Map[IdeEdge[T], V]
) {

  def apply(edge: IdeEdge[T]): V =
    edgeToFn(edge)

  lazy val keys: Set[IdeEdge[T]] = edgeToFn.keySet
}
