package ca.uwaterloo.ide

trait IdeFunction[F <: IdeFunction[F]] {

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