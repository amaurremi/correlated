package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.PathEdge

trait ExplodedGraphTypes extends SuperGraphTypes with FactTransform {

  /**
   * The type for propagated factoids (corresponds to elements of the set D)
   */
  type Fact

  /**
   * The type for IDE functions that correspond to the edges in the exploded supergraph
   */
  type IdeFunction <: IdeFunctionI

  /**
   * The type for a lattice element for the set L
   */
  type LatticeElem <: Lattice

  /**
   * A lattice for elements of the set L
   */
  trait Lattice {

    def ⊓(el: LatticeElem): LatticeElem

    override def equals(o: Any): Boolean
  }

  /**
   * An IDE function that corresponds to an edge in the exploded supergraph
   */
  trait IdeFunctionI {

    def apply(arg: LatticeElem): LatticeElem

    /**
     * Meet operator
     */
    def ⊓(f: IdeFunction): IdeFunction

    /**
     * Compose operator
     */
    def ◦(f: IdeFunction): IdeFunction

    /**
     * It's necessary to implement the equals method on IDE functions.
     */
    override def equals(obj: Any): Boolean
  }

  /**
   * A node in the exploded supergraph
   */
  trait IdeNode { // todo override equals?
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
        override lazy val isStartNode  = supergraph isEntry node
        override lazy val isReturnNode = supergraph isReturn node
        override lazy val isExitNode   = supergraph isExit node
        override lazy val isCallNode   = supergraph isCall node
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
