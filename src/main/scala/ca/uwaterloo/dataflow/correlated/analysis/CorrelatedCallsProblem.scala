package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.IMethod
import scala.collection.breakOut

trait CorrelatedCallsProblem extends IdeProblem { this: IfdsProblem =>

  type MultiMap = Map[Receiver, Set[Type]]

  override type LatticeElem = CorrelatedLatticeElem
  override type IdeFunction = CorrelatedFunction

  override val Bottom = ⊥
  override val Top    = ⊤
  override val Id     = ???
  override val λTop   = ???

  override def otherSuccEdges: IdeEdgeFn  = ???
  override def endReturnEdges: IdeEdgeFn  = ???
  override def callReturnEdges: IdeEdgeFn = ???
  override def callStartEdges: IdeEdgeFn  = ???

  sealed trait CorrelatedLatticeElem extends Lattice

  case class ReceiverToTypes(mapping: MultiMap) extends CorrelatedLatticeElem {
    /**
     * Defined as join. For two multi-maps M1 and M2, computes a multi-map M3
     * that maps M1's and M2's keys to the union of their value sets.
     */
    override def ⊓(el: CorrelatedLatticeElem): CorrelatedLatticeElem =
      el match {
        case ReceiverToTypes(mapping2) =>
          ReceiverToTypes(((mapping.keySet ++ mapping2.keySet) map {
            key =>
              val getTypes = (m: MultiMap) => m get key getOrElse Set.empty
              key -> (getTypes(mapping) ++ getTypes(mapping2))
          })(breakOut))
        case topOrBottom: CorrelatedLatticeElem               =>
          topOrBottom ⊓ this
      }
  }

  case object ⊥ extends CorrelatedLatticeElem {
    override def ⊓(el: CorrelatedLatticeElem): CorrelatedLatticeElem = el
  }

  case object ⊤ extends CorrelatedLatticeElem {
    override def ⊓(el: CorrelatedLatticeElem): CorrelatedLatticeElem = ⊤
  }

  trait CorrelatedFunction extends IdeFunctionI

  object UpdateMapFunction extends CorrelatedFunction {

    override def apply(arg: CorrelatedLatticeElem): CorrelatedLatticeElem = ???

    override def ◦(f: CorrelatedFunction): CorrelatedFunction = ???

    override def ⊓(f: CorrelatedFunction): CorrelatedFunction = ???
  }

  case class Receiver(valueNumber: Int, method: IMethod)
  case class Type()

  case class UpdateMap(receiver: Receiver, tpe: Type)
}
