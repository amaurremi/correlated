package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.StatsBenchmarkRunner._
import org.scalatest.FunSpec

class StatsSpec extends FunSpec {

  describe("Compute polymorphic calls") {

    it("doesn't have polymorphic calls") {
      val file = "ca/uwaterloo/dataflow/correlated/collector/inputPrograms/Poly1/Poly1.jar"
      val conf = "ca/uwaterloo/dataflow/correlated/collector/Poly1.conf"
      val stats = getCcStats("Poly1", "ca/uwaterloo/dataflow/correlated/collector/")
      assertResult(2)(stats.dispatchCallSiteNum)
      assertResult(0)(stats.polymorphicCallSiteNum)
    }

    it("has one polymorphic call") {
      val file = "ca/uwaterloo/dataflow/correlated/collector/inputPrograms/Poly2/Poly2.jar"
      val conf = "ca/uwaterloo/dataflow/correlated/collector/Poly2.conf"
      val stats = getCcStats("Poly1", "ca/uwaterloo/dataflow/correlated/collector/")
      assertResult(2)(stats.dispatchCallSiteNum)
      assertResult(1)(stats.polymorphicCallSiteNum)
    }
  }
}