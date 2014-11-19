package ca.uwaterloo.dataflow.correlated

import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.CGNode

import scalaz.Writer

package object collector {
  type MultiMap[K, V] = Map[K, Set[V]]
  type CallSite = (CallSiteReference, CGNode)
  type ReceiverToCallSites = MultiMap[Receiver, CallSite]
  type CorrelatedCallWriter[T] = Writer[CorrelatedCallStats, T]
}
