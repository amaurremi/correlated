package ca.uwaterloo.ide

trait IdeConstants { this: ExplodedGraphTypes =>

  /**
   * Represents λl.⊤
   */
  val Top: IdeFunction

  /**
   * Represents λl.l
   */
  val Id: IdeFunction

  /**
   * Representation of the Λ factoid
   */
  val zeroFact: Fact
}
