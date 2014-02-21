package ca.uwaterloo.ide

// todo make generic

sealed trait LatticeNum {
  def +(n:LatticeNum): LatticeNum
  def -(n: LatticeNum): LatticeNum
  def *(n: LatticeNum): LatticeNum
  def ⊓(n: LatticeNum): LatticeNum
  val inverse: LatticeNum
}

case object ⊤ extends LatticeNum {
  override def +(n: LatticeNum) = ⊤
  override def -(n: LatticeNum) = ⊤
  override def *(n: LatticeNum) = ⊤
  override def ⊓(n: LatticeNum) = n
  override val inverse = ⊤
}

case object ⊥ extends LatticeNum {
  override def +(n: LatticeNum) = ⊥
  override def -(n: LatticeNum) = ⊥
  override def *(n: LatticeNum) = ⊥
  override def ⊓(n: LatticeNum) = ⊥
  override val inverse = ⊥
}

case class Num(n: Long) extends LatticeNum {

  override def +(ln: LatticeNum) = ln match {
    case Num(n2) => Num(n + n2)
    case tb      => tb + this
  }

  override def -(ln: LatticeNum) = this + ln.inverse

  override def *(ln: LatticeNum) = ln match {
    case Num(n2) => Num(n * n2)
    case _       => ln * this
  }

  override val inverse: LatticeNum = Num(-n)

  override def ⊓(ln: LatticeNum) = ln match {
    case Num(n2) => if (n == n2) ln else ⊥
    case _       => ln ⊓ this
  }
}
