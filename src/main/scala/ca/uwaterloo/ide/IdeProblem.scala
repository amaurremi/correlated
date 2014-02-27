package ca.uwaterloo.ide

trait IdeProblem extends IdeTypes with IdeConstants with FactInfo with Supergraph with FlowFunctions {

  /**
   * The main method nodes that should be the entry points for the analysis
   */
  val entryPoints: Seq[Node] // todo don't like name
}
