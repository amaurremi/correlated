package ca.uwaterloo.ide.cp

import ca.uwaterloo.ide.analysis.PropagationTester
import ca.uwaterloo.ide.analysis.cp.CopyConstantPropagation
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {
    it("propagates constants intra-procedurally") {
      val ccs = new CopyConstantPropagation("LocalVars") with PropagationTester
      import ccs._

      val assignmentVals = getValsAtAssignments(inMain = true)
      val returnNodeVals = getValsAtReturn(inMain = true)
      assertResult(assignmentVals, "The assigned value should be propagated to the return node")(returnNodeVals)
    }

    it("propagates constants along the call-start edge") {
      val ccs = new CopyConstantPropagation("FunctionCall") with PropagationTester
      import ccs._

      val assignmentVals = getValsAtAssignments(inMain = true) map { _._2 }
      val returnNodeVals = getValsAtReturn(inMain = false) map { _._2 }
      assertResult(assignmentVals, "The assigned value in main should be propagated to the return node in f")(returnNodeVals)
    }

    it("sets its parameter to bottom, if same function is invoked with different constant arguments") {
      val ccs = new CopyConstantPropagation("MultipleFunctionCalls") with PropagationTester
      import ccs._

      val returnNodeVals = getValsAtReturn(inMain = false, expectedNumber = 3)
      assertResult(returnNodeVals.head._2)(⊥)
    }

    it("propagates constants along the end-return edge") {
      val ccs = new CopyConstantPropagation("ReturnConstant") with PropagationTester
      import ccs._

      val returnNodeVals = getValsAtReturn(inMain = true)
      val (vn, method) = getReturnVal
      assertResult(Num(vn, method), "The value returned in f should be propagated to main")(returnNodeVals.head._2)
    }

    it("propagates variable constants along the end-return edge") {
      val ccs = new CopyConstantPropagation("Return") with PropagationTester
      import ccs._

      val fAssignmentVals = getValsAtAssignments(inMain = false) map { _._2 }
      val returnNodeVals  = getValsAtReturn(inMain = true)
      assertResult(fAssignmentVals, "The constant-variable value returned in f should be propagated to main")(returnNodeVals)
    }

    it("assigns bottom to variables that have been assigned different values in if branches") {
      val ccs = new CopyConstantPropagation("Phi") with PropagationTester
      import ccs._

      val returnNodeVals = getValsAtReturn(inMain = true).head._2
      assertResult(returnNodeVals, "The constant-variable value returned in f should be propagated to main")(⊥)
    }

    it("propagate constant variables that have been assigned the same value in different if branches") {
      val ccs = new CopyConstantPropagation("PhiSame") with PropagationTester
      import ccs._

      val returnNodeVal = getValsAtReturn(inMain = true).head
      val assignmentVals = getValsAtAssignments(inMain = true, expectedNumber = 2).toSet
      assert(assignmentVals.size == 1, "There should be only one distinct assigned value")
      assertResult(assignmentVals.head, "The constant-variable value returned in f should be propagated to main")(returnNodeVal)
    }
  }
}
