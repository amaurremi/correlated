package ca.uwaterloo.dataflow.ifds.conversion

import ca.uwaterloo.dataflow.common.AbstractIdeToIfds

trait IdeToIfds extends AbstractIdeToIfds { this: IdeFromIfdsBuilder =>

  override def ifdsResult: Map[Node, Set[Fact]] =
    solvedResult.foldLeft(Map[Node, Set[Fact]]().empty withDefaultValue Set.empty[Fact]) {
      case (result, (XNode(n, f), Bottom)) =>
        result + (n -> (result(n) + f))
      case (result, _)                     =>
        result
    }
}
