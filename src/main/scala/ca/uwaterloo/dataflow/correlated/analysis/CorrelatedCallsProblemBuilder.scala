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

  val TypesTop: TypesLattice = SetType(Set.empty)

  override val Bottom = MapLatticeElem(TypesBottom, Map.empty[Receiver, TypesLattice])
  override val Top    = MapLatticeElem(TypesTop, Map.empty[Receiver, TypesLattice])
  override val Id     = SomeCorrelatedFunction(Map.empty[Receiver, ComposedTypes])
  override val λTop   = TopCorrelatedFunction

  case class Receiver(valueNumber: Int, method: IMethod)

  /**
   * Lattice elements of lattice L in the analysis. Represents functions from receivers to sets of types.
   */
  sealed trait MapLatticeElem extends Lattice[MapLatticeElem] {

    def default: TypesLattice

    def mapping: TypeMultiMap

    def hasEmptyMapping: Boolean = (default == TypesTop) || (mapping.values exists { _ == TypesTop })

    /**
     * Defined as join. For two multi-maps M1 and M2, computes a multi-map M3
     * that maps M1's and M2's keys to the union of their value sets.
     */
    override def ⊓(el: MapLatticeElem): MapLatticeElem = {
      val joinedMaps: TypeMultiMap = ((mapping.keySet ++ el.mapping.keySet) map {
        key =>
          key -> (mapping getOrElse (key, default)) ⊓ (el.mapping getOrElse (key, el.default))
      })(breakOut)
      MapLatticeElem(default ⊓ el.default, joinedMaps)
    }

    override def toString: String =
      if (mapping.isEmpty && default == TypesTop)
        "λr.{}"
      else if (mapping.isEmpty && default == TypesBottom)
        "λr.(all types)"
       else super.toString
  }

  object MapLatticeElem {

    def apply(default: TypesLattice, mapping: TypeMultiMap) =
      MapLatticeElemImpl(default, mapping filterNot { case (r, ts) => ts == default})

    def unapply(mle: MapLatticeElem): Option[(TypesLattice, TypeMultiMap)] =
      Some(mle.default, mle.mapping)

    case class MapLatticeElemImpl private[MapLatticeElem](
     default: TypesLattice,
     mapping: TypeMultiMap
   ) extends MapLatticeElem
  }

  sealed trait ComposedTypes {
    def intersectSet: TypesLattice
    def unionSet: TypesLattice
  }

  object ComposedTypes {

    def apply(intersectSet: TypesLattice, unionSet: TypesLattice): ComposedTypes =
      ComposedTypesImpl(intersectSet ⊓ unionSet, unionSet)

    def unapply(ct: ComposedTypes): Option[(TypesLattice, TypesLattice)] = Some(ct.intersectSet, ct.unionSet)

    case class ComposedTypesImpl private[ComposedTypes](
      intersectSet: TypesLattice,
      unionSet: TypesLattice
    ) extends ComposedTypes
  }

  sealed trait TypesLattice extends Lattice[TypesLattice] {
    def contains(tpe: Type): Boolean
  }

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

    override def contains(tpe: Type): Boolean = types contains tpe
  }

  case object TypesBottom extends TypesLattice {

    override def ⊓(el: TypesLattice) = TypesBottom

    override def ⊔(el: TypesLattice) = el

    override def contains(tpe: Type) = true
  }

  sealed trait CorrelatedFunction extends IdeFunctionI

  case class SomeCorrelatedFunction(updates: ComposedTypeMultiMap) extends CorrelatedFunction {

    override def apply(el: MapLatticeElem): MapLatticeElem =
      MapLatticeElem(
        el.default,
        updates.foldLeft(el.mapping) {
          case (m, (receiver, ComposedTypes(i, u))) =>
            m updated (receiver, ((m getOrElse (receiver, TypesBottom)) ⊔ i) ⊓ u)
        }
      )

    private[this] def operation(
      f: SomeCorrelatedFunction,
      onIntersect: (ComposedTypes, ComposedTypes) => TypesLattice,
      onUnion: (ComposedTypes, ComposedTypes) => TypesLattice,
      withDefault: (ComposedTypeMultiMap, Receiver) => ComposedTypes
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
          },
          (m, r) => m getOrElse (r, ComposedTypes(TypesBottom, TypesTop)))
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
          },
          (m, r) => m getOrElse (r, ComposedTypes(TypesBottom, TypesTop)))
        case TopCorrelatedFunction       =>
          f ⊓ this
      }

    override def toString: String =
      if (this == Id)
        "id"
      else super.toString
  }

  case object TopCorrelatedFunction extends CorrelatedFunction {

    override def apply(el: MapLatticeElem): MapLatticeElem = Top

    override def ◦(f: CorrelatedFunction): CorrelatedFunction = //TopCorrelatedFunction
      throw new UnsupportedOperationException("Top functions should not be composed")

    override def ⊓(f: CorrelatedFunction): CorrelatedFunction = f

    override def toString: String = "λl.top"
  }
}
