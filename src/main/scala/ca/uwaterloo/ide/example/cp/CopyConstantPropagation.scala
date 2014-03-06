package ca.uwaterloo.ide.example.cp

class CopyConstantPropagation(fileName: String) extends ConstantPropagation(fileName) {

  override type IdeFunction = CpFunction
  override type LatticeElem = CpLatticeElem

  override val Bottom: LatticeElem    = ⊥
  override val Top: LatticeElem       = ⊤
  override val Id: IdeFunction        = CpFunction(⊤)
  override val λTop: IdeFunction      = CpFunction(⊤) // todo correct?

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn = ???

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: EdgeFn = ???

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: EdgeFn = ???

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartEdges: EdgeFn = ???

  /**
   * Represents a function
   * λl . (a * l + b) ⊓ c
   * as described on p. 153 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
   * with applications to constant propagation"
   */
  case class CpFunction(c: LatticeElem) extends IdeFunctionI {

    override def apply(arg: LatticeElem): LatticeElem = arg ⊓ c

    /**
     * Meet operator
     */
    override def ⊓(f: CpFunction): CpFunction = CpFunction(c ⊓ f.c)

    override def ◦(f: CpFunction): CpFunction = ⊓(f) // todo correct?
  }

  trait CpLatticeElem extends Lattice {
    def ⊓(n: CpLatticeElem): CpLatticeElem
  }

  case object ⊤ extends CpLatticeElem {
    override def ⊓(n: CpLatticeElem) = n
  }

  case object ⊥ extends CpLatticeElem {
    override def ⊓(n: CpLatticeElem) = ⊥
  }

  case class Num(n: Long) extends CpLatticeElem {
    override def ⊓(ln: CpLatticeElem) = ln match {
      case Num(n2) => if (n == n2) ln else ⊥
      case _       => ln ⊓ this
    }
  }
}
