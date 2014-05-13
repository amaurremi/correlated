package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.correlated.collector.{CorrelatedCalls, Receiver}
import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import com.ibm.wala.classLoader.IClass
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

  val TypesTop: TypesLattice = SetType(Set.empty[Type])

  lazy val ccReceivers: Set[Receiver] = CorrelatedCalls(callGraph).receiverToCallSites.keys.toSet

  val composedTypesTop    = ComposedTypes(TypesTop, TypesTop)
  val composedTypesId     = ComposedTypes(TypesBottom, TypesTop)

  override val Bottom = MapLatticeElem(mapReceivers(TypesBottom))
  override val Top    = MapLatticeElem(mapReceivers(TypesTop))

  override val Id     = CorrelatedFunction(mapReceivers(composedTypesId))
  override val λTop   = CorrelatedFunction(mapReceivers(composedTypesTop))

  private[this] def mapReceivers[A](value: A): Map[Receiver, A] =
    (ccReceivers map {
      _ -> value
    })(breakOut)

  /**
   * Lattice elements of lattice L in the analysis. Represents functions from receivers to sets of types.
   */
  case class MapLatticeElem(mapping: TypeMultiMap) extends Lattice[MapLatticeElem] {

    /**
     * Defined as join. For two multi-maps M1 and M2, computes a multi-map M3
     * that maps M1's and M2's keys to the union of their value sets.
     */
    override def ⊓(el: MapLatticeElem): MapLatticeElem =
      MapLatticeElem((ccReceivers map {
        r =>
          r -> (mapping(r) ⊓ el.mapping(r))
      })(breakOut))

    def hasEmptyMapping: Boolean = mapping.values exists { _ == TypesTop }
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

  trait CorrelatedFunction extends IdeFunctionI {

    val updates: ComposedTypeMultiMap

    override def apply(el: MapLatticeElem): MapLatticeElem =
      MapLatticeElem(
        updates.foldLeft(el.mapping) {
          case (m, (receiver, ComposedTypes(i, u))) =>
            m updated (receiver, (m(receiver) ⊔ i) ⊓ u)
        }
      )

    override def ◦(f: CorrelatedFunction): CorrelatedFunction =
      CorrelatedFunction(
        (ccReceivers map {
          r =>
            val ComposedTypes(i1, u1) = updates(r)
            val ComposedTypes(i2, u2) = f.updates(r)
            r -> ComposedTypes(
              i1 ⊔ i2, (i1 ⊔ u2) ⊓ u1
            )
        })(breakOut)
      )

    override def ⊓(f: CorrelatedFunction): CorrelatedFunction =
      CorrelatedFunction(
        (ccReceivers map {
          r =>
            val ComposedTypes(i1, u1) = updates(r)
            val ComposedTypes(i2, u2) = f.updates(r)
            r -> ComposedTypes(
              i1 ⊓ i2, u1 ⊓ u2
            )
        })(breakOut))

    override def toString: String =
      if (this == Id)
        "id"
      else super.toString
  }

  object CorrelatedFunction {

    def apply(pairs: ComposedTypeMultiMap): CorrelatedFunction =
      CorrelatedFunctionImpl(
        if (pairs.size == ccReceivers.size)
          pairs
        else
          (ccReceivers map {
            r =>
              r -> (pairs getOrElse (r, composedTypesId))
          })(breakOut)
      )

    def unapply(ct: CorrelatedFunction): Option[ComposedTypeMultiMap] = Some(ct.updates)

    case class CorrelatedFunctionImpl private[CorrelatedFunction](
      updates: ComposedTypeMultiMap
    ) extends CorrelatedFunction
  }
}
