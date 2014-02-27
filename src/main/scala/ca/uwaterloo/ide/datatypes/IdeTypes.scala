package ca.uwaterloo.ide

trait IdeTypes {

  /**
   * Type of a node in the WALA supergraph
   */
  type Node

  /**
   * Type of a procedure for the WALA supergraph
   */
  type Procedure

  /**
   * The type for propagated factoids
   */
  type Fact

  /**
   * The type for IDE functions that correspond to the edges in the exploded supergraph
   */
  type IdeFunction <: IdeFunctionTrait[IdeFunction]
}
