package ca.uwaterloo.ide.example.constant

import ca.uwaterloo.ide._

/**
 * Represents a function
 * λl . (a * l + b) ⊓ c
 * as described on p. 153 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
 * with applications to constant propagation"
 */
case class ConstantPropagationF(a: Long, b: Long, c: LatticeElem) extends IdeFunctionTrait[ConstantPropagationF] {

  override def apply(arg: LatticeElem): LatticeElem = (Num(a) * arg + Num(b)) ⊓ c

  /**
   * Meet operator
   */
  override def ⊓(f: ConstantPropagationF): ConstantPropagationF =
    f match {
      case ConstantPropagationF(a2, b2, c2) =>
        if (a == a2 && b == b2)
          ConstantPropagationF(a, b, c ⊓ c2)
        else if (equiv(a2, b2, c2))
          this
        else
          ConstantPropagationF(1, 0, ⊥)
    }

  private def equiv(a2: Long, b2: Long, c2: LatticeElem): Boolean = {
    val l: Double = (b - b2) / (a2 - a)
    l.isWhole() && c == (Num(a * l.toInt + b) ⊓ c ⊓ c2)
  }

  override def ◦(f: ConstantPropagationF): ConstantPropagationF =
    f match {
      case ConstantPropagationF(a2, b2, c2) =>
        ConstantPropagationF(a * a2, a * b2 + b, (Num(a) * c2 + Num(b)) ⊓ c)
    }

  override def equals(obj: Any): Boolean =
    obj match {
      case ConstantPropagationF(_, _, ⊥) if c == ⊥ => true
      case _ => super.equals(obj)
    }
}
