package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.common.SuperGraphTypes
import ca.uwaterloo.dataflow.correlated.analysis.Receivers
import ca.uwaterloo.dataflow.correlated.collector.{Receiver, FakeReceiver, CorrelatedCalls, ReceiverI}

trait CcReceivers extends Receivers { this: SuperGraphTypes =>

  override def getCcReceivers: Set[ReceiverI] = CorrelatedCalls(callGraph).receiverToCallSites.keys.toSet
}
