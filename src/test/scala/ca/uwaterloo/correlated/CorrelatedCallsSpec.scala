package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.TestUtil._
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsSpec extends FunSpec {

  describe("CorrelatedCalls") {

    // note: CC stands for Correlated Call
    //       RC stands for Recursive Component

    it("has zero-stats for programs with an empty main method") {
      val ccs = getCcsForPointerAnalysisCallGraph("Nothing")
      assert(ccs.ccReceiverNum == 0, "cc receivers")
      assert(ccs.ccSiteNum == 0, "cc sites")
      assert(ccs.dispatchCallSiteNum == 0, "dispatch sites")
      assert(ccs.rcCcReceiverNum == 0, "cc receivers in rcs")
      assert(ccs.rcNodeNum == 0, "nodes in rcs")
      assert(ccs.rcNum == 0, "rcs")
//      assert(ccs.cgNodeNum == 0, "cg nodes")                 // todo this outputs 9
//      assert(ccs.totalCallSiteNum == 0, "total call sites")  // todo this outputs 9
    }

    it("doesn't include library calls") {
      val ccs = getCcsForPointerAnalysisCallGraph("LibraryCall")
//      assert(ccs.cgNodeNum == ???)                           // todo
//      println(ccs.totalCallSiteNum == ???)                   // todo
    }

    it("returns no CCs for programs where each receiver has at most one invocation, excluding private and static calls") {
      val ccs = getCcsForPointerAnalysisCallGraph("NoCcs")
      assert(ccs.ccReceiverNum == 0, "no cc receivers")
      assert(ccs.ccSiteNum == 0, "no cc sites")
    }

    it("detects CCs, including invocations on 'this'") {
      val ccs = getCcsForPointerAnalysisCallGraph("CcsPresent")
      assert(ccs.ccReceiverNum == 4, "cc receivers")
      assert(ccs.ccSiteNum == 8, "cc sites")
      assert(ccs.rcNum == 0, "rcs")
    }

    it("detects recursive and mutually recursive methods") {
      val ccs = getCcsForPointerAnalysisCallGraph("Rec")
      assert(ccs.rcNum == 2, "recursive components")
      assert(ccs.rcCcReceiverNum == 1, "cc receiver in recursive component")
    }
  }
}
