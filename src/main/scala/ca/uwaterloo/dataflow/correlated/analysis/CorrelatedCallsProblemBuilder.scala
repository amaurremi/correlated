package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import com.ibm.wala.classLoader.IMethod
import scala.Predef.Map
import scala.Predef.Set
import scala.collection._

trait CorrelatedCallsProblemBuilder extends IdeProblem {

  type MultiMap = Map[Receiver, Set[Type]]

  override type LatticeElem = CorrelatedLatticeElem
  override type IdeFunction = CorrelatedFunction

  override val Bottom = ReceiverToTypes(Map.empty)
  override val Top    = ⊤
  override val Id     = ???
  override val λTop   = ???

  sealed trait CorrelatedLatticeElem extends Lattice

  case class ReceiverToTypes(mapping: MultiMap) extends CorrelatedLatticeElem {

    /**
     * Defined as join. For two multi-maps M1 and M2, computes a multi-map M3
     * that maps M1's and M2's keys to the union of their value sets.
     */
    override def ⊓(el: CorrelatedLatticeElem): CorrelatedLatticeElem =
      el match {
        case ReceiverToTypes(mapping2) =>
          ReceiverToTypes(joinMultiMaps(mapping, mapping2))
        case topOrBottom               =>
          topOrBottom ⊓ this
      }
  }

  case object ⊤ extends CorrelatedLatticeElem {
    override def ⊓(el: CorrelatedLatticeElem): CorrelatedLatticeElem = ⊤
  }

  case class CorrelatedFunction(updates: MultiMap) extends IdeFunctionI {

    override def apply(el: CorrelatedLatticeElem): CorrelatedLatticeElem =
      ReceiverToTypes(
        el match {
          case ReceiverToTypes(mapping) =>
            updates.foldLeft(mapping) {
              case (m, (receiver, types)) =>
                m updated (receiver, m getOrElse (receiver, Set.empty) intersect types)
            }
          case `⊤`                      =>
            updates
        }
      )

    override def ◦(f: CorrelatedFunction): CorrelatedFunction =
      CorrelatedFunction(intersectMultiMapValues(updates, f.updates))

    /**
     * Defined as join.
     */
    override def ⊓(f: CorrelatedFunction): CorrelatedFunction =
      CorrelatedFunction(joinMultiMaps(updates, f.updates))
  }

  case class Receiver(valueNumber: Int, method: IMethod)

  case class Type()

  private[this] def joinMultiMaps(m1: MultiMap, m2: MultiMap): MultiMap =
    operationOnMultiMaps({ _ intersect _ }, m1, m2)

  private[this] def intersectMultiMapValues(m1: MultiMap, m2: MultiMap): MultiMap = // todo is it correct to intersect only values (not keys)?
    operationOnMultiMaps({ _ ++ _ }, m1, m2)

  private[this] def operationOnMultiMaps(
    operation: (Set[Type], Set[Type]) => Set[Type],
    m1: MultiMap,
    m2: MultiMap
  ): MultiMap =
    ((m1.keySet ++ m2.keySet) map {
      key =>
        val getTypes = (m: MultiMap) => m getOrElse(key, Set.empty)
        key -> operation(getTypes(m1), getTypes(m2))
    })(breakOut)
}
