package ca.uwaterloo.ide

// todo: pass n, d2, and m instead of the whole edge. return d3 plus function
case class FactFunPair[T, V <: IdeFunction[V]](
  d2: Fact,
  edgeFn: V
)

trait EdgeFunctions[T, V <: IdeFunction[V]] {

  def callStartFns: EdgeFn[T, V]

  def callStartD2s: (T, Fact, T) => Seq[Fact] =
    (n1, d1, n2) =>
      callStartFns(n1, d1, n2) map { _.d2 }

  def callReturnEdges: EdgeFn[T, V]

  def endReturnEdges: EdgeFn[T, V]

  def otherSuccEdges: EdgeFn[T, V]
}
