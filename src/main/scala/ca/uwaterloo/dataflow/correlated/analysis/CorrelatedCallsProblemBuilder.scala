package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.correlated.collector.ReceiverI
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
trait CorrelatedCallsProblemBuilder extends IdeProblem with Receivers {

  type TypeMultiMap         = Map[ReceiverI, TypesLattice]
  type ComposedTypeMultiMap = Map[ReceiverI, ComposedTypes]
  type Type                 = IClass

  override type LatticeElem = MapLatticeElem
  override type IdeFunction = CorrelatedFunctionI

  val TypesTop: TypesLattice = SetType(Set.empty[Type])

  val composedTypesTop    = ComposedTypes(TypesTop, TypesTop)
  val composedTypesBottom = ComposedTypes(TypesBottom, TypesBottom)
  val composedTypesId     = ComposedTypes(TypesBottom, TypesTop)

  override val Bottom = MapLatticeElem(mapReceivers(TypesBottom))
  override val Top    = MapLatticeElem(mapReceivers(TypesTop))

  override val Id     = CorrelatedIdFunction
  override val λTop   = CorrelatedTopFunction

  def mapReceivers[A](value: A): Map[ReceiverI, A] =
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

    override def toString: String =
      if (this == Top)
        "top (empty set of types)"
      else if (this == Bottom)
        "bottom (all types)"
      else super.toString
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

    override def toString =
      if (this == TypesTop)
        "TypesTop"
      else super.toString
  }

  case object TypesBottom extends TypesLattice {

    override def ⊓(el: TypesLattice) = TypesBottom

    override def ⊔(el: TypesLattice) = el
  }

  sealed trait CorrelatedFunctionI extends IdeFunctionI

  sealed trait CorrelatedFunction extends CorrelatedFunctionI {

    val updates: ComposedTypeMultiMap

    override def apply(el: MapLatticeElem): MapLatticeElem =
      MapLatticeElem(
        updates.foldLeft(el.mapping) {
          case (m, (receiver, ComposedTypes(i, u))) =>
            m updated (receiver, (m(receiver) ⊔ i) ⊓ u)
        }
      )

    override def ◦(f: CorrelatedFunctionI): CorrelatedFunctionI =
      f match {
        case CorrelatedIdFunction         =>
          this
        case CorrelatedTopFunction        =>
          val pairs: ComposedTypeMultiMap = (ccReceivers map {
            r =>
              r -> ComposedTypes(TypesTop, updates.getOrElse(r, composedTypesId).unionSet)
          })(breakOut)
          CorrelatedFunction(pairs)
        case CorrelatedFunction(fUpdates) =>
          val recToTypes: ComposedTypeMultiMap = ((updates.keys ++ fUpdates.keys) map {
            r =>
              val ComposedTypes(i1, u1) = updates getOrElse (r, composedTypesId)
              val ComposedTypes(i2, u2) = fUpdates getOrElse (r, composedTypesId)
              r -> ComposedTypes(
                i1 ⊔ i2, (i1 ⊔ u2) ⊓ u1
              )
          })(breakOut)
          CorrelatedFunction(recToTypes filter {
            _._2 != composedTypesId
          })
      }

    override def ⊓(f: CorrelatedFunctionI): CorrelatedFunctionI =
      f match {
        case CorrelatedIdFunction         =>
          CorrelatedIdFunction ⊓ this
        case CorrelatedTopFunction        =>
          this
        case CorrelatedFunction(fUpdates) =>
          CorrelatedFunction(
            ((updates.keys ++ fUpdates.keys) map {
              r =>
                val ComposedTypes(i1, u1) = updates getOrElse (r, composedTypesId)
                val ComposedTypes(i2, u2) = fUpdates getOrElse (r, composedTypesId)
                r -> ComposedTypes(
                  i1 ⊓ i2, u1 ⊓ u2
                )
            })(breakOut))
      }
  }

  object CorrelatedFunction {

    def apply(pairs: ComposedTypeMultiMap): CorrelatedFunctionI = {
      if (pairs.isEmpty)
        CorrelatedIdFunction
      else {
        val nonIdUpdates = pairs filter {
          _._2 != composedTypesId
        }
        if (nonIdUpdates.isEmpty)
          CorrelatedIdFunction
        else
          CorrelatedFunctionImpl(nonIdUpdates)
      }
    }

    def unapply(ct: CorrelatedFunction): Option[ComposedTypeMultiMap] = Some(ct.updates)

    case class CorrelatedFunctionImpl private[CorrelatedFunction](
      updates: ComposedTypeMultiMap
    ) extends CorrelatedFunction
  }

  object CorrelatedIdFunction extends CorrelatedFunctionI {

    override def apply(el: LatticeElem): LatticeElem = el

    override def ⊓(f: IdeFunction): IdeFunction =
      f match {
        case CorrelatedIdFunction        =>
          f
        case CorrelatedTopFunction       =>
          this
        case CorrelatedFunction(updates) =>
          CorrelatedFunction(
            (updates.keys map {
              r =>
                r -> ComposedTypes(TypesBottom, updates(r).unionSet)
            })(breakOut))
      }

    override def ◦(f: IdeFunction): IdeFunction = f

    override def toString: String = "id"
  }

  object CorrelatedTopFunction extends CorrelatedFunctionI {

    override def apply(arg: LatticeElem): LatticeElem = Top

    override def ⊓(f: IdeFunction): IdeFunction = f

    override def ◦(f: IdeFunction): IdeFunction = this
  }
}
