package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph

trait IdeNode[T] {
  val n: T
  val d: Fact 
}

case class CallNode[T](n: T, d: Fact) extends IdeNode[T]
case class ReturnNode[T](n: T, d: Fact) extends IdeNode[T]
case class StartNode[T](n: T, d: Fact) extends IdeNode[T]
case class ExitNode[T](n: T, d: Fact) extends IdeNode[T]
case class OtherNode[T](n: T, d: Fact) extends IdeNode[T]

object IdeNode {
  
  def apply[T, P](
    n: T, 
    d: Fact,
    supergraph: ISupergraph[T, P]
  ): IdeNode[T] = // todo: is it bad to use implicits like that?
    if (supergraph.isEntry(n)) // todo: in WALA, there is no explicit entry node
      StartNode(n, d)
    else if (supergraph.isCall(n))
      CallNode(n, d)
    else if (supergraph.isExit(n)) // todo: in WALA, there is no explicit exit node
      ExitNode(n, d)
    else if (supergraph.isReturn(n))
      ReturnNode(n, d)
    else 
      OtherNode(n, d)
}