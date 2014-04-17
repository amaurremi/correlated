package ca.uwaterloo.dataflow.ide.analysis.problem

import ca.uwaterloo.dataflow.common.ExplodedGraphTypes

trait IdeExplodedGraphTypes extends ExplodedGraphTypes {

  /**
   * The type for IDE functions that correspond to the edges in the exploded supergraph
   */
  type IdeFunction <: IdeFunctionI

  /**
   * The type for a lattice element for the set L
   */
  type LatticeElem <: Lattice[LatticeElem]

  /**
   * A lattice for elements of the set L
   */
  trait Lattice[L <: Lattice[L]] {

    def ⊓(el: L): L

    def ⊔(el: L): L = throw new UnsupportedOperationException("Join operation not defined.")

    override def equals(o: Any): Boolean
  }

  /**
   * An IDE function that corresponds to an edge in the exploded supergraph
   */
  trait IdeFunctionI {

    def apply(arg: LatticeElem): LatticeElem

    /**
     * Meet operator
     */
    def ⊓(f: IdeFunction): IdeFunction

    /**
     * Compose operator
     */
    def ◦(f: IdeFunction): IdeFunction

    /**
     * It's necessary to implement the equals method on IDE functions.
     */
    override def equals(obj: Any): Boolean
  }
}
