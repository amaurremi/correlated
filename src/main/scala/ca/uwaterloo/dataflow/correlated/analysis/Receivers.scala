package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.correlated.collector.{FakeReceiver, ReceiverI}

trait Receivers {

  lazy val ccReceivers: Set[ReceiverI] = getCcReceivers + FakeReceiver

  def getCcReceivers: Set[ReceiverI]
}
