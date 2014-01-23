package ca.uwaterloo

import scala.collection.immutable
import scalaz.Writer

package object correlated {
  type ImmutableMultiMap[K, V] = immutable.Map[K, immutable.Set[V]]
  type CorrelatedCallWriter[T] = Writer[CorrelatedCalls, T]
}
