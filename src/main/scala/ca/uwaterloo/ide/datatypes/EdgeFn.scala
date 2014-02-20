package ca.uwaterloo.ide

final class EdgeFn[T, V <: IdeFunction[V]](
  edgeToFn: Map[IdeEdge[T], V]
) {

  def apply(edge: IdeEdge[T]): V =
    edgeToFn(edge)

  private[this] lazy val keys: Set[IdeEdge[T]] = edgeToFn.keySet

  /**
   * All edges with a given source.
   */
  lazy val edgesWithSource: IdeNode[T] => Set[IdeEdge[T]] =
    (source: IdeNode[T]) =>
      keys filter {
        _.source == source
      }

  /**
   * All edges with a given target node.
   */
  lazy val edgesWithTarget: IdeNode[T] => Set[IdeEdge[T]] =
    (target: IdeNode[T]) =>
      keys filter {
        _.target == target
      }
}
