package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.AbstractIdeToIfds

/**
 * Take the result of a correlated calls analysis and return the result of the source IFDS analysis.
 */
trait CorrelatedCallsToIfds extends AbstractIdeToIfds with CorrelatedCallsProblem {

  /**
   * Converts the IDE correlated calls result into an improved IFDS result.
   * For a given node n, consider fact f that is mapped to a set S. If the enclosing class of n
   * is contained in S, then f is reachable.
   */
  override def ifdsResult: Map[Node, Set[Fact]] =
    solvedResult.foldLeft(Map[Node, Set[Fact]]() withDefaultValue Set.empty[Fact]) {
      case (result, (XNode(n, f), l)) =>
        if (l.hasEmptyMapping || f == Lambda)
          result + (n -> result(n))
        else
          result + (n -> (result(n) + f))
    }
}
