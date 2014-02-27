package ca.uwaterloo.ide

trait FactInfo extends IdeTypes { // todo rename
  /**
   * Transform into an integer for WALA usage
   */
  val factToInt: Fact => Int

  /**
   * Transform integer to a fact to use facts from WALA
   */
  val intToFact: Int => Fact
}
