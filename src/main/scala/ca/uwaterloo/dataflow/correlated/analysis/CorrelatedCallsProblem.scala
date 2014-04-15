package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.IMethod
import scala.collection.breakOut

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
    /**
     * Defined as join. For two multi maps M1 and M2, computes a multi map M3
     * that maps M1's and M2's keys to the union of their value sets.
     */
    override def ⊓(el: ReceiverToTypes): ReceiverToTypes =
      ReceiverToTypes(((mapping.keySet ++ el.mapping.keySet) map {
        key =>
          val getTypes = (m: ReceiverToTypes) => m.mapping get key getOrElse Set.empty
          key -> (getTypes(this) ++ getTypes(el))
      })(breakOut))
  }

  trait CorrelatedFunction extends IdeFunctionI

  case class Receiver(valueNumber: Int, method: IMethod)
  case class Type()
}
