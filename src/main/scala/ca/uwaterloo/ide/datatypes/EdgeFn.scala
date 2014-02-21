package ca.uwaterloo.ide

// todo: pass n, d2, and m instead of the whole edge. return d3 plus function
final class EdgeFn[T, V <: IdeFunction[V]](
  edgeToFn: Map[IdeEdge[T], V]
) {

  def apply(edge: IdeEdge[T]): V =
    edgeToFn(edge)

  lazy val keys: Set[IdeEdge[T]] = edgeToFn.keySet
}
