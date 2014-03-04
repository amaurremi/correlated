package ca.uwaterloo.ide

trait Lattices {

  trait Lattice[L <: Lattice[L]] {

    def ⊓(el: L): L

    override def equals(o: Any): Boolean
  }

  /**
   * The type for a lattice element
   */
  type LatticeElem <: Lattice[LatticeElem]
}