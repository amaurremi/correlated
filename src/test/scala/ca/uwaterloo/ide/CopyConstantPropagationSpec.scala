package ca.uwaterloo.ide.example.cp

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {
    it("propagates constants intra-procedurally") {
      val ccs = new CopyConstantPropagation("LocalVars") with CopyConstantPropagationTester
      import ccs._

      val assignmentVals = getValsAtAssignments(inMain = true)
      val returnNodeVals = getValsAtReturn(inMain = true)
      assertResult(assignmentVals, "The assigned value should be propagated to the return node")(returnNodeVals)
    }

    it("propagates constants along the call-start edge") {
      val ccs = new CopyConstantPropagation("FunctionCall") with CopyConstantPropagationTester
      import ccs._

      val assignmentVals = getValsAtAssignments(inMain = true)
      val returnNodeVals = getValsAtReturn(inMain = false)
      assertResult(assignmentVals, "The assigned value in main should be propagated to the return node in f")(returnNodeVals)
    }

    it("sets its parameter to bottom, if same function is invoked with different constant arguments") {
      val ccs = new CopyConstantPropagation("MultipleFunctionCalls") with CopyConstantPropagationTester
      import ccs._

      val returnNodeVals = getValsAtReturn(inMain = false, expectedNumber = 3)
      assertResult(returnNodeVals.head)(⊥)
    }

    it("propagates constants along the end-return edge") {
      val ccs = new CopyConstantPropagation("ReturnConstant") with CopyConstantPropagationTester
      import ccs._

      val returnNodeVals = getValsAtReturn(inMain = true)
      val returnedByProc = getReturnVal
      assertResult(returnedByProc, "The value returned in f should be propagated to main")(returnNodeVals.head)
    }

    it("propagates variable constants along the end-return edge") {
      val ccs = new CopyConstantPropagation("Return") with CopyConstantPropagationTester
      import ccs._

      val fAssignmentVals = getValsAtAssignments(inMain = false)
      val returnNodeVals  = getValsAtReturn(inMain = true)
      assertResult(fAssignmentVals, "The constant-variable value returned in f should be propagated to main")(returnNodeVals)
    }
  }
}
