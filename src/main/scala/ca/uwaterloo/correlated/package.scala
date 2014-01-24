package ca.uwaterloo

import scalaz.Writer

package object correlated {
  type MultiMap[K, V] = Map[K, Set[V]]
  type CorrelatedCallWriter[T] = Writer[CorrelatedCalls, T]
}
