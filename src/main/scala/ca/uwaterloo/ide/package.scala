package ca.uwaterloo

import scala.collection.mutable

package object ide {

  type JumpFn[T, V <: IdeFunction[V]] = mutable.Map[IdeEdge[T], V]

  type Values[T] = mutable.Map[IdeNode[T], LatticeNum]

  type EdgeFn[T, V <: IdeFunction[V]] = (T, Fact, T) => Seq[FactFunPair[T, V]]
}
