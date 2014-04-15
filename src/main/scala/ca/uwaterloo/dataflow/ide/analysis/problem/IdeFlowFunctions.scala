package ca.uwaterloo.dataflow.ide

/**
 * Defines functions that, for an edge (n, d1) -> (m, d2),
 * for which we know n, d1, and m, return all
 * d2s plus the corresponding edge IDE functions.
 */
trait IdeFlowFunctions { this: IdeExplodedGraphTypes with IdeConstants =>

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
  type IdeEdgeFn = (XNode, Node) => Set[FactFunPair]

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  def callStartEdges: IdeEdgeFn

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  def callReturnEdges: IdeEdgeFn

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  def endReturnEdges: IdeEdgeFn

  /**
   * Functions for all other (inter-procedural) edges.
   */
  def otherSuccEdges: IdeEdgeFn

  /**
   * Helper function analogous to callStartFns, but returns only the factoids, without the edge functions.
   */
  def callStartD2s: (XNode, Node) => Set[Fact] =
    (node1, n2) =>
      callStartEdges(node1, n2) map { _.d2 }

  final val idFactFunPairSet = (d: Fact) => Set(FactFunPair(d, Id))
}
