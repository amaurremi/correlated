package ca.uwaterloo.dataflow.common

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ide.analysis.solver.IdeSolver
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem

trait AbstractIdeToIfds extends IdeProblem with IfdsProblem with IdeSolver {

  def ifdsResult: Map[Node, Set[Fact]]
}
