package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.correlated.analysis.Receivers
import ca.uwaterloo.dataflow.correlated.collector.{FakeReceiver, CorrelatedCalls, ReceiverI}
import ca.uwaterloo.dataflow.common.SuperGraphTypes

trait CcReceivers extends Receivers { this: SuperGraphTypes =>

  override val ccReceivers: Set[ReceiverI] = CorrelatedCalls(callGraph).receiverToCallSites.keys.toSet + FakeReceiver
}
