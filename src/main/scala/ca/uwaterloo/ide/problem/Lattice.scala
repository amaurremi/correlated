package ca.uwaterloo.ide

trait Lattice[L <: Lattice[L]] {

  def ⊓(el: L): L

  override def equals(o: Any): Boolean
}
