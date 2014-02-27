package ca.uwaterloo.ide

/**
 * Defines functions that, for an edge (n, d1) -> (m, d2),
 * for which we know n, d1, and m, return all
 * d2s plus the corresponding edge IDE functions.
 */
trait FlowFunctions { this: IdeTypes =>

  case class FactFunPair(
    d2: Fact,
    edgeFn: IdeFunction
  )

  /**
   *
   */
  type EdgeFn = (Node, Fact, Node) => Seq[FactFunPair]

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  def callStartFns: EdgeFn

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  def callReturnEdges: EdgeFn

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  def endReturnEdges: EdgeFn

  /**
   * Functions for all other (inter-procedural) edges.
   */
  def otherSuccEdges: EdgeFn

  /**
   * Helper function analogous to callStartFns, but returns only the factoids, without the edge functions.
   */
  def callStartD2s: (Node, Fact, Node) => Seq[Fact] =
    (n1, d1, n2) =>
      callStartFns(n1, d1, n2) map { _.d2 }
}
