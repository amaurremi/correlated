package ca.uwaterloo.ide

/**
 * Represents a function
 * λl . (a * l + b) ∩ c
 * as described on p. 153 of Sagiv, Reps, Horwitz, "Precise interprocedural dataflow analysis
 * with applications to constant propagation"
 */
case class IdeFunction(a: Long, b: Long, c: LatticeNum) { // todo: make more generic (not only dealing with Long's)

  def apply(arg: LatticeNum): LatticeNum = (Num(a) * arg + Num(b)) meet c

  /**
   * Meet operator
   */
  def ⊓(f: IdeFunction): IdeFunction = {
    case IdeFunction(a2, b2, c2) =>
      if (a == a2 && b == b2)
        IdeFunction(a, b, c meet c2)
      else if (equiv(a2, b2, c2))
        this
      else
        IdeFunction(1, 0, ⊥)
  }

  private def equiv(a2: Long, b2: Long, c2: LatticeNum): Boolean = {
    val l: Double = (b - b2) / (a2 - a)
    l.isWhole() && c == (Num(a * l.toInt + b) meet c meet c2)
  }

  def ◦(f: IdeFunction): IdeFunction = {
    case IdeFunction(a2, b2, c2) =>
      IdeFunction(a * a2, a * b2 + b, (Num(a) * c2 + Num(b)) meet c)
  }

  override def equals(obj: Any): Boolean =
    obj match {
      case IdeFunction(_, _, ⊥) if c == ⊥ => true
      case _ => super.equals(obj)
    }
}

object IdeFunction {

  val Top: IdeFunction = IdeFunction(0, 0, ⊤)

  val Id: IdeFunction = IdeFunction(1, 0, ⊤)
}