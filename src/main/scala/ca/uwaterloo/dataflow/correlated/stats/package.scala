package ca.uwaterloo.dataflow.correlated

import com.ibm.wala.classLoader.CallSiteReference
import scalaz.Writer

package object stats {
  type MultiMap[K, V] = Map[K, Set[V]]
  type ReceiverToCallSites = MultiMap[Receiver, CallSiteReference]
  type CorrelatedCallWriter[T] = Writer[CorrelatedCalls, T]
}
