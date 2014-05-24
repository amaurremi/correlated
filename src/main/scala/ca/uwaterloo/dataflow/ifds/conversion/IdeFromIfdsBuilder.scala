package ca.uwaterloo.dataflow.ifds.conversion

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem

trait IdeFromIfdsBuilder extends IdeProblem { this: IfdsProblem =>

  override type LatticeElem = IfdsLatticeElem
  override type IdeFunction = IfdsFunction

  override val Bottom: LatticeElem = IfdsBottom
  override val Top: LatticeElem    = IfdsTop
  override val Id: IdeFunction     = IfdsIdFunction
  override val λTop: IdeFunction   = IfdsTopFunction

  override def otherSuccEdges: IdeOtherEdgeFn      = zipWithIdOther(ifdsOtherSuccEdges)
  override def otherSuccEdgesPhi: IdeOtherEdgeFn   = zipWithIdOther(ifdsOtherSuccEdgesPhi)
  override def endReturnEdges: IdeEdgeFn           = zipWithId(ifdsEndReturnEdges)
  override def callReturnEdges: IdeEdgeFn          = zipWithId(ifdsCallReturnEdges)
  override def callStartEdges: IdeEdgeFn           = zipWithId(ifdsCallStartEdges)

  private[this] def zipWithId(f: IfdsEdgeFn): IdeEdgeFn =
    (ideN1, d1) =>
      f(ideN1, d1) map { FactFunPair(_, IfdsIdFunction) }

  private[this] def zipWithIdOther(f: IfdsOtherEdgeFn): IdeOtherEdgeFn =
    ideN1 =>
      f(ideN1) map { FactFunPair(_, IfdsIdFunction) }

  trait IfdsLatticeElem extends Lattice[IfdsLatticeElem]

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
