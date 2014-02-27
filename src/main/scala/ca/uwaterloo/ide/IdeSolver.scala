package ca.uwaterloo.ide

class IdeSolver { this: IdeProblem with JumpFuncs with ComputeValues =>

  // todo: modify edges in super graph?
  def solve() = {
    val jumpFuncs = computeJumpFuncs
    computeValues(jumpFuncs)
  }
}
