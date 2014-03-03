package ca.uwaterloo.ide

/**
 * Defines functions that, for an edge (n, d1) -> (m, d2),
 * for which we know n, d1, and m, return all
 * d2s plus the corresponding edge IDE functions.
 */
trait FlowFunctions { this: ExplodedGraphTypes =>

  case class FactFunPair(
    d2: Fact,
    edgeFn: IdeFunction
  )

  /**
   * Given partial information for an edge:
   *   n, d1, m
   * returns d2 such that there exists an edge in the exploded graph:
   *   (n, d1) -> (m, d2)
   * along with the corresponding edge functions.
   */
  type EdgeFn = (IdeNode, Node) => Seq[FactFunPair]

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
  def callStartD2s: (IdeNode, Node) => Seq[Fact] =
    (node1, n2) =>
      callStartFns(node1, n2) map { _.d2 }
}
