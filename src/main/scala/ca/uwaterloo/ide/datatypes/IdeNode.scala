package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph

sealed trait IdeNode[T] {
  val n: T
  val d: Fact
  val isStartNode: Boolean
  val isExitNode: Boolean
  val isReturnNode: Boolean
  val isCallNode: Boolean
}

object IdeNode {

  def apply[T, P](
    node: T,
    fact: Fact
  )(
    implicit supergraph: ISupergraph[T, P]
  ): IdeNode[T] =
    new IdeNode[T] {
      override val n = node
      override val d = fact
      override val isStartNode = supergraph isEntry node
      override val isReturnNode = supergraph isReturn node
      override val isExitNode = supergraph isExit node
      override val isCallNode = supergraph isCall node
    }
}
