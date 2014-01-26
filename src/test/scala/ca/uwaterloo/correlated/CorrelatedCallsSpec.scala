package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.TestUtil._
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsSpec extends FunSpec {

  describe("CorrelatedCalls") {

    // note: CC stands for Correlated Call

    it("returns no CCs for programs where each receiver has at most one invocation, excluding private calls") {
      val ccs = getCcsForPointerAnalysisCallGraph("NoCcs")
      assert(ccs.ccReceiverNum == 0, "no cc receivers")
      assert(ccs.ccSiteNum == 0, "no cc sites")
    }

    it("detects CCs, including invocations on 'this'") {
      val ccs = getCcsForPointerAnalysisCallGraph("CcsPresent")
      assert(ccs.ccReceiverNum == 3, "cc receivers")
      assert(ccs.ccSiteNum == 6, "cc sites")
    }

    it("detects recursive and mutually recursive methods") {
      val ccs = getCcsForPointerAnalysisCallGraph("Rec")
      assert(ccs.sccNum == 2, "recursive components")
      assert(ccs.sccCcReceiverNum == 1, "cc receiver in recursive component")
    }
  }
}
