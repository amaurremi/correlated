package ca.uwaterloo.ide

trait IdeNodes extends Supergraph {

  sealed trait IdeNode {
    val n: Node
    val d: Fact
    val isStartNode: Boolean
    val isExitNode: Boolean
    val isReturnNode: Boolean
    val isCallNode: Boolean
  }

  object IdeNode {

    def apply(
      node: Node,
      fact: Fact
    ): IdeNode =
      new IdeNode {
        override val n = node
        override val d = fact
        override lazy val isStartNode = supergraph isEntry node
        override lazy val isReturnNode = supergraph isReturn node
        override lazy val isExitNode = supergraph isExit node
        override lazy val isCallNode = supergraph isCall node
      }
  }
}