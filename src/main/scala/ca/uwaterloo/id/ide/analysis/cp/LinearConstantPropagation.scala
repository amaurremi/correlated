package ca.uwaterloo.id.ide.analysis.cp

class LinearConstantPropagation(fileName: String) extends ConstantPropagation(fileName) {

  override type IdeFunction = CpFunction
  override type LatticeElem = CpLatticeElem

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = CpFunction(1, 0, ⊤)
  override val λTop: IdeFunction   = CpFunction(0, 0, ⊤)

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: IdeEdgeFn = ???

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: IdeEdgeFn = ???

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: IdeEdgeFn = ???

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartEdges: IdeEdgeFn = ???

  /**
   * Represents a function
   * λl . (a * l + b) ⊓ c
   * as described on p. 153 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
   * with applications to constant propagation"
   */
  case class CpFunction(a: Long, b: Long, c: LatticeElem) extends IdeFunctionI {

    override def apply(arg: LatticeElem): LatticeElem = (Num(a) * arg + Num(b)) ⊓ c

    /**
     * Meet operator
     */
    override def ⊓(f: CpFunction): CpFunction =
      f match {
        case CpFunction(a2, b2, c2) =>
          if (a == a2 && b == b2)
            CpFunction(a, b, c ⊓ c2)
          else if (equiv(a2, b2, c2))
            this
          else
            CpFunction(1, 0, ⊥)
      }

    private def equiv(a2: Long, b2: Long, c2: LatticeElem): Boolean = {
      val l: Double = (b - b2) / (a2 - a)
      l.isWhole && c == (Num(a * l.toInt + b) ⊓ c ⊓ c2)
    }

    override def ◦(f: CpFunction): CpFunction =
      f match {
        case CpFunction(a2, b2, c2) =>
          CpFunction(a * a2, a * b2 + b, (Num(a) * c2 + Num(b)) ⊓ c)
      }

    override def equals(obj: Any): Boolean =
      obj match {
        case CpFunction(_, _, ⊥) if c == ⊥ => true
        case _ => super.equals(obj)
      }
  }

  trait CpLatticeElem extends Lattice {
    def +(n:CpLatticeElem): CpLatticeElem
    def -(n: CpLatticeElem): CpLatticeElem
    def *(n: CpLatticeElem): CpLatticeElem
    def ⊓(n: CpLatticeElem): CpLatticeElem
    val inverse: CpLatticeElem
  }

  case object ⊤ extends CpLatticeElem {
    override def +(n: CpLatticeElem) = ⊤
    override def -(n: CpLatticeElem) = ⊤
    override def *(n: CpLatticeElem) = ⊤
    override def ⊓(n: CpLatticeElem) = n
    override val inverse = ⊤
  }

  case object ⊥ extends CpLatticeElem {
    override def +(n: CpLatticeElem) = ⊥
    override def -(n: CpLatticeElem) = ⊥
    override def *(n: CpLatticeElem) = ⊥
    override def ⊓(n: CpLatticeElem) = ⊥
    override val inverse = ⊥
  }

  case class Num(n: Long) extends CpLatticeElem {

    override def +(ln: CpLatticeElem) = ln match {
      case Num(n2) => Num(n + n2)
      case tb      => tb + this
    }

    override def -(ln: CpLatticeElem) = this + ln.inverse

    override def *(ln: CpLatticeElem) = ln match {
      case Num(n2) => Num(n * n2)
      case _       => ln * this
    }

    override val inverse: CpLatticeElem = Num(-n)

    override def ⊓(ln: CpLatticeElem) = ln match {
      case Num(n2) => if (n == n2) ln else ⊥
      case _       => ln ⊓ this
    }
  }
}
