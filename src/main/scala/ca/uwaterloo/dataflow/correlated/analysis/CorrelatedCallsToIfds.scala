package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.AbstractIdeToIfds

/**
 * Take the result of a correlated calls analysis and return the result of the source IFDS analysis.
 */
trait CorrelatedCallsToIfds extends AbstractIdeToIfds with CorrelatedCallsProblem { // todo Order?

  /**
   * Converts the IDE correlated calls result into an improved IFDS result.
   * For a given node n, consider fact f that is mapped to a set S. If the enclosing class of n
   * is contained in S, then f is reachable.
   */
  // todo If the fact is an ArrayElement, we make it reachable. Is that correct?
  override def ifdsResult: Map[Node, Set[Fact]] =
    solvedResult.foldLeft(Map[Node, Set[Fact]]() withDefaultValue Set.empty[Fact]) {
      case (result, (XNode(n, Lambda), _))                  =>
        result + (n -> result(n))
      case (result, (XNode(n, f), l)) if !l.hasEmptyMapping =>
        result + (n -> (result(n) + f))
    }
}
