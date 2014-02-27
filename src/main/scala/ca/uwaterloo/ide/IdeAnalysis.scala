package ca.uwaterloo.ide

trait IdeAnalysis extends IdeSolver with IdeProblem with Supergraph with JumpFuncs with ComputeValues with ExplodedGraphInfo with IdeNodes with IdeEdges {

}
