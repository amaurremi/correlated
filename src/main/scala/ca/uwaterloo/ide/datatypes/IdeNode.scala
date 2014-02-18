package ca.uwaterloo.ide

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