package ca.uwaterloo.dataflow.ide

import ca.uwaterloo.dataflow.ide.analysis.problem.{IdeExplodedGraphTypes, IdeConstants}

trait SecretAssertionMap { this: IdeConstants with IdeExplodedGraphTypes =>

  /**
   * A map from method names to lattice elements. For a given assertion method, indicates what
   * lattice element should be expected.
   */
  def assertionMap: Map[String, LatticeElem]
}
