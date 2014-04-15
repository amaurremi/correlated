package ca.uwaterloo.dataflow.ide

import ca.uwaterloo.dataflow.common.TraverseGraph

trait IdeSolver extends JumpFuncs with ComputeValues with TraverseGraph { this: IdeProblem =>

  /**
   * Runs the IDE instance defined in IdeProblem.
   */
  lazy val solvedResult: Map[XNode, LatticeElem] = {
    // computeJumpFuncs corresponds to Phase I of the algorithm, computeValues corresponds to Phase II.
    computeValues(computeJumpFuncs)
  }
}
