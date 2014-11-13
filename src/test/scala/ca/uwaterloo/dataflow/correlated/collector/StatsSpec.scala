package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.StatsBenchmarkRunner._
import org.scalatest.FunSpec

class StatsSpec extends FunSpec {

  val path = "ca/uwaterloo/dataflow/correlated/collector/"

  describe("Compute polymorphic calls") {

    it("Poly1: doesn't have polymorphic calls") {
      val stats = getCcStats("Poly1", path)
      assertResult(2, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(0, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }

    it("Poly2: has one polymorphic call") {
      val stats = getCcStats("Poly2", path)
      assertResult(2, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(1, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }

    it("Poly3: has 2 polymorphic calls") {
      val stats = getCcStats("Poly3", path)
      assertResult(2, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(2, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }

    it("Poly4: has 3 polymorphic calls") {
      val stats = getCcStats("Poly4", path)
      assertResult(3, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(3, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }

    it("Poly5: has 10 polymorphic calls") {
      val stats = getCcStats("Poly5", path)
      assertResult(10, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(10, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }

    it("Poly6: has 6 polymorphic calls") {
      val stats = getCcStats("Poly6", path)
      assertResult(6, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(6, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }

    it("Poly7: has 7 polymorphic calls") {
      val stats = getCcStats("Poly7", path)
      assertResult(7, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(7, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }

    it("Poly8: has 1 polymorphic call (parameter passing)") {
      val stats = getCcStats("Poly8", path)
      assertResult(1, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(1, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }
  }

  describe("Precision of polymorphic call detection") {
    it("Poly9") {
      val stats = getCcStats("Poly9", path)
      assertResult(2, "dispatch call sites")(stats.dispatchCallSiteNum)
      assertResult(0, "polymorphic call sites")(stats.polymorphicCallSiteNum)
    }
  }
}
