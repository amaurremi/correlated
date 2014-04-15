package ca.uwaterloo.dataflow.ide.analysis.problem

trait IdeConstants { this: IdeExplodedGraphTypes =>

  /**
   * Represents λl.⊤
   */
  val λTop: IdeFunction

  /**
   * Represents λl.l
   */
  val Id: IdeFunction

  /**
   * Representation of the Λ (zero) factoid
   */
  val Λ: Fact

  /**
   * Lattice top element
   */
  val Top: LatticeElem

  /**
   * Lattice bottom element
   */
  val Bottom: LatticeElem
}
