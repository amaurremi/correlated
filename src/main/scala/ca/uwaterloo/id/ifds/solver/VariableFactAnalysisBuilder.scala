package ca.uwaterloo.id.ifds.solver

import ca.uwaterloo.id.common.VariableFacts
import ca.uwaterloo.id.ide.{IdeSolver, IdeProblem}
import ca.uwaterloo.id.ifds.IfdsProblem

trait VariableFactAnalysisBuilder extends IdeProblem with IdeSolver with IfdsProblem with VariableFacts {

  override type LatticeElem = IfdsLatticeElem
  override type IdeFunction = IfdsFunction

  override val Bottom: LatticeElem = IfdsBottom
  override val Top: LatticeElem    = IfdsTop
  override val Id: IdeFunction     = IfdsIdFunction
  override val λTop: IdeFunction   = IfdsTopFunction

  override def otherSuccEdges: IdeEdgeFn  = zipWithId(ifdsOtherSuccEdges)
  override def endReturnEdges: IdeEdgeFn  = zipWithId(ifdsEndReturnEdges)
  override def callReturnEdges: IdeEdgeFn = zipWithId(ifdsCallReturnEdges)
  override def callStartEdges: IdeEdgeFn  = zipWithId(ifdsCallStartEdges)

  private[this] def zipWithId(f: IfdsEdgeFn): IdeEdgeFn =
    (ideN1, d1) =>
      f(XNode(ideN1.n, ideN1.d), d1) map { FactFunPair(_, IfdsIdFunction) }

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
