package ca.uwaterloo.ide

trait FactTransform { this: ExplodedGraphTypes =>
  
  /**
   * Transform into an integer for WALA usage
   */
  val factToInt: Fact => Int

  /**
   * Transform integer to a T
   */
  val intToFact: Int => Fact
}
