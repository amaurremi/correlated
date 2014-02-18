package ca.uwaterloo.ide

trait IdeNode[T] {
  val n: T
  val d: Int
}

case class CallNode[T](n: T, d: Int) extends IdeNode[T]
case class ReturnNode[T](n: T, d: Int) extends IdeNode[T]
case class StartNode[T, P](n: T, d: Int, proc: P) extends IdeNode[T]
case class ExitNode[T](n: T, d: Int) extends IdeNode[T]
case class ProcNode[T](n: T, d: Int) extends IdeNode[T]

object IdeNode {
  
  def apply[T](n: T, d: Int): IdeNode[T] = ???
}