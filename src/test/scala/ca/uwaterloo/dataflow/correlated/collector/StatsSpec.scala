package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.StatsBenchmarkRunner._
import org.scalatest.FunSpec

class StatsSpec extends FunSpec {

  describe("Compute polymorphic calls") {

    val path = "ca/uwaterloo/dataflow/correlated/collector/"

    it("doesn't have polymorphic calls") {
      val stats = getCcStats("Poly1", path)
      assertResult(2)(stats.dispatchCallSiteNum)
      assertResult(0)(stats.polymorphicCallSiteNum)
    }

    it("has one polymorphic call") {
      val stats = getCcStats("Poly2", path)
      assertResult(2)(stats.dispatchCallSiteNum)
      assertResult(1)(stats.polymorphicCallSiteNum)
    }

    it("has 2 polymorphic calls") {
      val stats = getCcStats("Poly3", path)
      assertResult(2)(stats.dispatchCallSiteNum)
      assertResult(2)(stats.polymorphicCallSiteNum)
    }

    it("has 3 polymorphic calls") {
      val stats = getCcStats("Poly4", path)
      assertResult(3)(stats.dispatchCallSiteNum)
      assertResult(3)(stats.polymorphicCallSiteNum)
    }
  }
}