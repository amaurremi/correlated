package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.correlated.collector.ReceiverI

trait Receivers {

  val ccReceivers: Set[ReceiverI] // todo should be lazy somehow
}
