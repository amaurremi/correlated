package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.TestUtil._
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsSpec extends FunSpec {

  describe("CorrelatedCalls") {

    it("returns no CCs for programs where each receiver has at most one invocation, excluding private calls") {
      val ccs = getCcsForPointerAnalysisCallGraph("noCcs")
      assert(ccs.ccReceiverNum == 0, "no cc receivers")
      assert(ccs.ccSites.size == 0, "no cc sites")
    }

    it("detects recursive and mutually recursive methods") {
      val ccs = getCcsForPointerAnalysisCallGraph("rec")
      assert(ccs.sccNum == 2, "2 recursive components")
      assert(ccs.sccCcReceiverNum == 1, "1 cc receiver in recursive component")
    }
  }
}
