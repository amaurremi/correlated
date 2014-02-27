package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.PathEdge

trait ExplodedGraphTypes extends SuperGraphTypes with FactTransform {

  /**
   * The type for propagated factoids
   */
  type Fact

  /**
   * The type for IDE functions that correspond to the edges in the exploded supergraph
   */
  type IdeFunction <: IdeFunctionTrait[IdeFunction]

  /**
   * A node in the exploded supergraph
   */
  trait IdeNode {
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

  /**
   * An edge in the exploded supergraph
   */
  case class IdeEdge(source: IdeNode, target: IdeNode)

  object IdeEdge {
    def apply(pathEdge: PathEdge[Node]): IdeEdge =
      IdeEdge(
        IdeNode(pathEdge.getEntry, intToFact(pathEdge.getD1)),
        IdeNode(pathEdge.getTarget, intToFact(pathEdge.getD2)))
  }
}
