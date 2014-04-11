package ca.uwaterloo.id.conversion

import ca.uwaterloo.id.ide.IdeProblem
import ca.uwaterloo.id.ifds.IfdsProblem
import com.ibm.wala.dataflow.IFDS.ISupergraph

object Ifds2Ide {

  def convert(ifdsProblem: IfdsProblem): IdeProblem = new IdeProblem {

    override type Fact = ifdsProblem.Fact
    override type Procedure = ifdsProblem.Procedure
    override type Node = ifdsProblem.Node

    override type LatticeElem = IfdsLatticeElem
    override type IdeFunction = IfdsFunction

    override val Λ: Fact = ifdsProblem.O
    override val entryPoints: Seq[Node] = ifdsProblem.entryPoints
    override val supergraph: ISupergraph[Node, Procedure] = ifdsProblem.supergraph

    override val Bottom: LatticeElem = IfdsBottom
    override val Top: LatticeElem = IfdsTop
    override val Id: IdeFunction = IfdsIdFunction
    override val λTop: IdeFunction = IfdsTopFunction

    /**
     * Functions for all other (inter-procedural) edges.
     */
    override def otherSuccEdges: IdeEdgeFn = zipWithId

    /**
     * Functions for inter-procedural edges from an end node to the return node of the callee function.
     */
    override def endReturnEdges: IdeEdgeFn = zipWithId

    /**
     * Functions for intra-procedural edges from a call to the corresponding return edges.
     */
    override def callReturnEdges: IdeEdgeFn = zipWithId

    /**
     * Functions for inter-procedural edges from a call node to the corresponding start edges.
     */
    override def callStartEdges: IdeEdgeFn = zipWithId

    private[this] def zipWithId: IdeEdgeFn =
      (ideN1, d1) =>
        ifdsProblem.otherSuccEdges(ifdsProblem.XNode(ideN1.n, ideN1.d), d1) map { FactFunPair(_, IfdsIdFunction) }

    trait IfdsLatticeElem extends Lattice

    case object IfdsTop extends IfdsLatticeElem {
      override def ⊓(el: IfdsLatticeElem): IfdsLatticeElem = el
    }

    case object IfdsBottom extends IfdsLatticeElem {
      override def ⊓(el: IfdsLatticeElem): IfdsLatticeElem = IfdsBottom
    }

    trait IfdsFunction extends IdeFunctionI

    case object IfdsIdFunction extends IfdsFunction {

      override def apply(el: IfdsLatticeElem): IfdsLatticeElem = el

      override def ◦(f: IfdsFunction): IfdsFunction = f

      override def ⊓(f: IfdsFunction): IfdsFunction = IfdsIdFunction
    }

    case object IfdsTopFunction extends IfdsFunction {

      override def apply(el: IfdsLatticeElem): IfdsLatticeElem = IfdsTop

      override def ◦(f: IfdsFunction): IfdsFunction = IfdsTopFunction

      override def ⊓(f: IfdsFunction): IfdsFunction = f
    }
  }
}
