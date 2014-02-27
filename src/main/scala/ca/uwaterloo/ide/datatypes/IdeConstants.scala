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

  // todo remove
  val nonZeroFacts: Set[Fact]

  /**
   * Representation of the Λ factoid
   */
  val zeroFact: Fact
}
