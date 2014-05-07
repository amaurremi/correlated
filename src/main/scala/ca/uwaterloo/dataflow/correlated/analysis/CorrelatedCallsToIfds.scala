package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.AbstractIdeToIfds

/**
 * Take the result of a correlated calls analysis and return a more precise result of the source IFDS analysis.
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
      case (result, (XNode(n, ArrayElement), _))                       =>
        result + (n -> (result(n) + ArrayElement))
      case (result, (XNode(n, f: Field), _))                           =>
        result + (n -> (result(n) + f)) // todo I think that's wrong
      case (result, (XNode(n, v@Variable(m, el)), l))
        if l get Receiver(el, m) contains n.getMethod.getDeclaringClass =>
          result + (n -> (result(n) + v))
      case (result, _)                                                  =>
          result
    }
}
