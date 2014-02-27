package ca.uwaterloo.ide

/**
 * An IDE function that corresponds to an edge in the exploded supergraph
 */
trait IdeFunctionTrait[F <: IdeFunctionTrait[F]] {

  def apply(arg: LatticeNum): LatticeNum 

  /**
   * Meet operator
   */
  def ⊓(f: F): F

  /**
   * Compose operator
   */
  def ◦(f: F): F

  /**
   * It's necessary to implement the equals method on IDE functions.
   */
  override def equals(obj: Any): Boolean
}