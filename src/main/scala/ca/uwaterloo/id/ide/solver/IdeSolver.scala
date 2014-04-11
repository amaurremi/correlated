package ca.uwaterloo.id.ide

trait IdeSolver extends JumpFuncs with ComputeValues with TraverseGraph { this: IdeProblem =>

  /**
   * Runs the IDE analysis defined in IdeProblem.
   */
  lazy val solvedResult: Map[XNode, LatticeElem] = {
    // computeJumpFuncs corresponds to Phase I of the algorithm, computeValues corresponds to Phase II.
    computeValues(computeJumpFuncs)
  }
}
