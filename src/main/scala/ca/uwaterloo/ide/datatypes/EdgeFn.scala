package ca.uwaterloo.ide

final class EdgeFn[T, V <: IdeFunction[V]](
  edgeToFn: Map[IdeEdge[T], V]
) {

  def apply(edge: IdeEdge[T]): V =
    edgeToFn(edge)

  private[this] lazy val keys: Set[IdeEdge[T]] = edgeToFn.keySet

  lazy val edgesWithSource =
    (source: IdeNode[T]) =>
      keys filter {
        _.source == source
      }
}
