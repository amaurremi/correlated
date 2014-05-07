package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import com.ibm.wala.classLoader.{IClass, IMethod}
import scala.collection._

/**
 * In this trait,
 *   ⊓ is defined as union
 *   ⊔ is defined as intersect
 *   ⊥ (bottom) refers to the least precise element of a lattice (e.g. "all types")
 *   ⊤ (top) refers to the most precise element of a lattice (e.g. "empty set")
 */
trait CorrelatedCallsProblemBuilder extends IdeProblem {

  type TypeMultiMap         = Map[Receiver, TypesLattice]
  type ComposedTypeMultiMap = Map[Receiver, ComposedTypes]
  type Type                 = IClass

  override type LatticeElem = MapLatticeElem
  override type IdeFunction = CorrelatedFunction

  override val Bottom = ⊥
  override val Top    = ReceiverToTypes(Map.empty)
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
        case ⊥                         =>
          ⊥ ⊓ this
      }

    private[this] def joinMultiMaps(m1: TypeMultiMap, m2: TypeMultiMap): TypeMultiMap =
      ((m1.keySet ++ m2.keySet) map {
        key =>
          val getTypes: (TypeMultiMap => TypesLattice) = m => m getOrElse (key, TypesTop)
          key -> getTypes(m1) ⊓ getTypes(m2)
      })(breakOut)

    override def toString: String =
      if (this == Top) "top (empty set of types)"
      else super.toString
  }

  case object ⊥ extends MapLatticeElem {
    override def ⊓(el: MapLatticeElem): MapLatticeElem = ⊥
    override def toString: String = "bottom (all types)"
  }

  sealed trait ComposedTypes {
    def intersectSet: TypesLattice
    def unionSet: TypesLattice
  }

  object ComposedTypes { // todo will equals work?

    def apply(intersectSet: TypesLattice, unionSet: TypesLattice): ComposedTypes =
      ComposedTypesImpl(intersectSet ⊓ unionSet, unionSet)

    def unapply(ct: ComposedTypes): Option[(TypesLattice, TypesLattice)] = Some(ct.intersectSet, ct.unionSet)

    case class ComposedTypesImpl private[ComposedTypes](
      intersectSet: TypesLattice,
      unionSet: TypesLattice
    ) extends ComposedTypes
  }

  sealed trait TypesLattice extends Lattice[TypesLattice]

  case class SetType(types: Set[Type]) extends TypesLattice {

    override def ⊔(typeLattice: TypesLattice) =
      typeLattice match {
        case SetType(types2) => SetType(types intersect types2)
        case TypesBottom     => TypesBottom ⊔ this
      }

    override def ⊓(typeLattice: TypesLattice) =
      typeLattice match {
        case SetType(types2) => SetType(types ++ types2)
        case TypesBottom     => TypesBottom ⊓ this
      }
  }

  case object TypesBottom extends TypesLattice {

    override def ⊓(el: TypesLattice) = TypesBottom

    override def ⊔(el: TypesLattice) = el
  }

  val TypesTop: TypesLattice = SetType(Set.empty)

  private[this] def withDefault(m: ComposedTypeMultiMap, r: Receiver): ComposedTypes =
    m getOrElse (r, ComposedTypes(TypesBottom, TypesTop))

  sealed trait CorrelatedFunction extends IdeFunctionI

  case class SomeCorrelatedFunction(updates: ComposedTypeMultiMap) extends CorrelatedFunction {

    override def apply(el: MapLatticeElem): MapLatticeElem =
      ReceiverToTypes(
        el match {
          case ReceiverToTypes(mapping) =>
            updates.foldLeft(mapping) {
              case (m, (receiver, ComposedTypes(i, u))) =>
                m updated (receiver, ((m getOrElse (receiver, TypesBottom)) ⊔ i) ⊓ u)
            }
          case ⊥ =>
            updates map {
              case (r, ComposedTypes(i, u)) =>
                r -> (i ⊓ u)
            }
        }
      )

    private[this] def operation(
      f: SomeCorrelatedFunction,
      onIntersect: (ComposedTypes, ComposedTypes) => TypesLattice,
      onUnion: (ComposedTypes, ComposedTypes) => TypesLattice
    ): CorrelatedFunction = {
      val newUpdates = (updates.keySet ++ f.updates.keySet).foldLeft(updates) {
        case (m, receiver) =>
          val t1 = withDefault(updates, receiver)
          val t2 = withDefault(f.updates, receiver)
          m + (receiver -> ComposedTypes(onIntersect(t1, t2), onUnion(t1, t2)))
      }
      SomeCorrelatedFunction(newUpdates)
    }

    override def ◦(f: CorrelatedFunction): CorrelatedFunction =
      f match {
        case fun: SomeCorrelatedFunction =>
          operation(fun, {
            _.intersectSet ⊔ _.intersectSet
          }, {
            (t1, t2) => (t1.intersectSet ⊔ t2.unionSet) ⊓ t1.unionSet
          })
        case TopCorrelatedFunction       =>
          TopCorrelatedFunction
      }

    override def ⊓(f: CorrelatedFunction): CorrelatedFunction =
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

  case object TopCorrelatedFunction extends CorrelatedFunction {

    override def apply(el: MapLatticeElem): MapLatticeElem = Top

    override def ◦(f: CorrelatedFunction): CorrelatedFunction = TopCorrelatedFunction

    override def ⊓(f: CorrelatedFunction): CorrelatedFunction = f
  }
}