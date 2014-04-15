package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.IMethod

trait CorrelatedCallsProblem extends IdeProblem { this: IfdsProblem =>

  type MultiMap = Map[Receiver, Set[Type]]

  override type LatticeElem = ReceiverToTypes
  override type IdeFunction = CorrelatedFunction

  override val Bottom: LatticeElem = ???
  override val Top: LatticeElem    = ???
  override val Id: IdeFunction     = ???
  override val λTop: IdeFunction   = ???

  override def otherSuccEdges: IdeEdgeFn  = ???
  override def endReturnEdges: IdeEdgeFn  = ???
  override def callReturnEdges: IdeEdgeFn = ???
  override def callStartEdges: IdeEdgeFn  = ???

  trait CorrelatedLatticeElem extends Lattice

  case class ReceiverToTypes(mapping: MultiMap) extends CorrelatedLatticeElem {
    override def ⊓(el: ReceiverToTypes): ReceiverToTypes =
      ???
  }

  trait CorrelatedFunction extends IdeFunctionI

  case class Receiver(valueNumber: Int, method: IMethod)
  case class Type()
}
