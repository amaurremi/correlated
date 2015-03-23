package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.common.{Time, SuperGraphTypes}
import ca.uwaterloo.dataflow.correlated.analysis.Receivers
import ca.uwaterloo.dataflow.correlated.collector.{CorrelatedCallStats, ReceiverI}

trait CcReceivers extends Receivers { this: SuperGraphTypes =>

  override def getCcReceivers: Set[ReceiverI] =
    CorrelatedCallStats(callGraph).receiverToCallSites.keys.toSet
}
