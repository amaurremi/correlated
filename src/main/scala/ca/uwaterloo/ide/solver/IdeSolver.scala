package ca.uwaterloo.ide

trait IdeSolver extends JumpFuncs with ComputeValues with TraverseGraph { this: IdeProblem =>

  /**
   * Runs the IDE analysis defined in IdeProblem.
   */
  def solve(): Map[IdeNode, LatticeElem] = {
    // computeJumpFuncs corresponds to Phase I of the algorithm, computeValues corresponds to Phase II.
    computeValues(computeJumpFuncs)
  }
}
