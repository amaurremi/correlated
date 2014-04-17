package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import com.ibm.wala.classLoader.{IClass, IMethod}
import scala.collection._

trait CorrelatedCallsProblemBuilder extends IdeProblem {

  type TypeMultiMap         = Map[Receiver, TypesLattice]
  type ComposedTypeMultiMap = Map[Receiver, ComposedTypesLattice]
  type Type                 = IClass

  override type LatticeElem = MapLatticeElem
  override type IdeFunction = CorrelatedFunction

  override val Bottom = ReceiverToTypes(Map.empty)
  override val Top    = ⊤
  override val Id     = SomeCorrelatedFunction(Map.empty)
  override val λTop   = TopCorrelatedFunction

  case class Receiver(valueNumber: Int, method: IMethod)

  sealed trait MapLatticeElem extends Lattice[MapLatticeElem]

  case class ReceiverToTypes(mapping: TypeMultiMap) extends MapLatticeElem {
    /**
     * Defined as join. For two multi-maps M1 and M2, computes a multi-map M3
     * that maps M1's and M2's keys to the union of their value sets.
     */
    override def ⊓(el: MapLatticeElem): MapLatticeElem =
      el match {
        case ReceiverToTypes(mapping2) =>
          ReceiverToTypes(joinMultiMaps(mapping, mapping2))
        case topOrBottom =>
          topOrBottom ⊓ this
      }

    private[this] def joinMultiMaps(m1: TypeMultiMap, m2: TypeMultiMap): TypeMultiMap =
      ((m1.keySet ++ m2.keySet) map {
        key =>
          val getTypes: (TypeMultiMap => TypesLattice) = m => m getOrElse (key, TypesBottom)
          key -> getTypes(m1) ⊓ getTypes(m2)
      })(breakOut)
  }
  
  case object ⊤ extends MapLatticeElem {
    override def ⊓(el: MapLatticeElem): MapLatticeElem = ⊤
  }

  case class ComposedTypesLattice(intersectSet: TypesLattice, unionSet: TypesLattice) extends Lattice[ComposedTypesLattice] {

    override def ⊔(el: ComposedTypesLattice): ComposedTypesLattice =
      ComposedTypesLattice(intersectSet ⊔ el.intersectSet, unionSet ⊔ el.unionSet)

    override def ⊓(el: ComposedTypesLattice): ComposedTypesLattice =
      ComposedTypesLattice(intersectSet ⊓ el.intersectSet, unionSet ⊓ el.unionSet)
  }

  sealed trait TypesLattice extends Lattice[TypesLattice]

  private[this] case class SetType(types: Set[Type]) extends TypesLattice {

    /**
     * Defined as intersect.
     */
    override def ⊔(typeLattice: TypesLattice) =
      typeLattice match {
        case SetType(types2) => SetType(types intersect types2)
        case TypeTop => TypeTop ⊔ this
      }

    /**
     * Defined as union.
     */
    override def ⊓(typeLattice: TypesLattice) =
      typeLattice match {
        case SetType(types2) => SetType(types ++ types2)
        case TypeTop => TypeTop ⊓ this
      }
  }

  private[this] case object TypeTop extends TypesLattice {
    override def ⊓(el: TypesLattice) = TypeTop

    override def ⊔(el: TypesLattice) = el
  }

  private[this] val TypesBottom: TypesLattice = SetType(Set.empty)

  private[this] def withDefault(m: ComposedTypeMultiMap, r: Receiver): ComposedTypesLattice =
    m getOrElse (r, ComposedTypesLattice(TypeTop, TypesBottom))
 
  sealed trait CorrelatedFunction extends IdeFunctionI
  
  case class SomeCorrelatedFunction(updates: ComposedTypeMultiMap) extends CorrelatedFunction {

    override def apply(el: MapLatticeElem): MapLatticeElem =
      ReceiverToTypes(
        el match {
          case ReceiverToTypes(mapping) =>
            updates.foldLeft(mapping) {
              case (m, (receiver, ComposedTypesLattice(i, u))) =>
                m updated (receiver, ((m getOrElse (receiver, TypeTop)) ⊔ i) ⊓ u) // todo check if that's correct
            }
          case ⊤ =>
            updates map {
              case (r, ComposedTypesLattice(i, u)) =>
                r -> (i ⊓ u)
            }
        }
      )

    private[this] def operation(
      f: SomeCorrelatedFunction,
      onIntersect: (ComposedTypesLattice, ComposedTypesLattice) => TypesLattice,
      onUnion: (ComposedTypesLattice, ComposedTypesLattice) => TypesLattice
    ): CorrelatedFunction = {
      val newUpdates = (updates.keySet ++ f.updates.keySet).foldLeft(updates) {
        case (m, receiver) =>
          val t1 = withDefault(updates, receiver)
          val t2 = withDefault(f.updates, receiver)
          m + (receiver -> ComposedTypesLattice(onIntersect(t1, t2), onUnion(t1, t2)))
      }
      SomeCorrelatedFunction(newUpdates)
    }

    override def ◦(f: CorrelatedFunction): CorrelatedFunction = // todo not sure
      f match {
        case fun: SomeCorrelatedFunction =>
          operation(fun, {
            _.intersectSet ⊔ _.intersectSet
          }, {
            (t1, t2) => (t1.intersectSet ⊔ t2.unionSet) ⊓ t1.unionSet
          })
        case TopCorrelatedFunction       =>
          f
      }

    /**
     * Defined as union.
     */
    override def ⊓(f: CorrelatedFunction): CorrelatedFunction = // todo not sure
      f match {
        case fun: SomeCorrelatedFunction =>
          operation(fun, {
            _.intersectSet ⊓ _.intersectSet
          }, {
            _.unionSet ⊓ _.unionSet
          })
        case TopCorrelatedFunction       =>
          f ⊓ this
      }
  }
  
  case object TopCorrelatedFunction extends CorrelatedFunction { // todo not sure about this trait

    override def apply(el: MapLatticeElem): MapLatticeElem = ⊤

    override def ◦(f: CorrelatedFunction): CorrelatedFunction = TopCorrelatedFunction

    override def ⊓(f: CorrelatedFunction): CorrelatedFunction = TopCorrelatedFunction
  }
}