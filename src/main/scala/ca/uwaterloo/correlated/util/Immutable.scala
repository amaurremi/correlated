package ca.uwaterloo.correlated.util

import scala.collection.JavaConverters._
import scala.collection.mutable
import ca.uwaterloo.correlated.ImmutableMultiMap

object Immutable {

  def convert[T](iterator: java.util.Iterator[T]) = iterator.asScala
}

object MultiMap {
  def empty[K, V] = MultiMap(mutable.Map[K, mutable.Set[V]]())

  def apply[K, V](mutableMap: mutable.Map[K, mutable.Set[V]]): ImmutableMultiMap[K, V] =
    (mutableMap map {
      case (k, v) => (k, v.toSet)
    }).toMap
}
