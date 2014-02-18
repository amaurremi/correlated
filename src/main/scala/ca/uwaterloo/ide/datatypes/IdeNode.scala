package ca.uwaterloo.ide

case class Fact(n: Int)

object Fact {
  val zero = Fact(0)
}

trait IdeNode[T] {
  val n: T
  val d: Fact 
}

case class CallNode[T](n: T, d: Fact) extends IdeNode[T]
case class ReturnNode[T](n: T, d: Fact) extends IdeNode[T]
case class StartNode[T](n: T, d: Fact) extends IdeNode[T]
case class ExitNode[T](n: T, d: Fact) extends IdeNode[T]
case class ProcNode[T](n: T, d: Fact) extends IdeNode[T]

object IdeNode {
  
  def apply[T](n: T, d: Fact): IdeNode[T] = ???
}