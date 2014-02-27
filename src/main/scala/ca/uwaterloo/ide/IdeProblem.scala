package ca.uwaterloo.ide

trait IdeProblem extends ExplodedGraphTypes with FlowFunctions with IdeConstants {

  /**
   * The main method nodes that should be the entry points for the analysis
   */
  val entryPoints: Seq[Node] // todo don't like name
}
