package ca.uwaterloo.dataflow.ifds.conversion

import ca.uwaterloo.dataflow.common.{Time, AbstractIdeToIfds}

trait IdeToIfds extends AbstractIdeToIfds {

  override def ifdsResult: Map[Node, Set[Fact]] =
      solvedResult.foldLeft(Map[Node, Set[Fact]]().empty withDefaultValue Set.empty[Fact]) {
        case (result, (XNode(NormalNode(n), f), Bottom)) if f != Λ =>
          result + (n -> (result(n) + f))
        case (result, _)                                           =>
          result
      }
}
