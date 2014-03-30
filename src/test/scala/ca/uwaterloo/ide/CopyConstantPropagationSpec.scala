package ca.uwaterloo.ide.example.cp

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {
   /* it("propagates constants intra-procedurally") {
      val ccs = new CopyConstantPropagation("LocalVars") with ConstantPropagationTester
      import ccs._

      val assignmentVals = getAssignmentVals(inMain = true)
      val returnNodeVals = getReturnVals(inMain = true)
      assertResult(assignmentVals, "The assigned value should be propagated to the return node")(returnNodeVals)
    }

    it("propagates constants along the call-start edge") {
      val ccs = new CopyConstantPropagation("FunctionCall") with ConstantPropagationTester
      import ccs._

      val assignmentVals = getAssignmentVals(inMain = true)
      val returnNodeVals = getReturnVals(inMain = false)
      assertResult(assignmentVals, "The assigned value in main should be propagated to the return node in f")(returnNodeVals)
    }*/

    it("propagates constants along the end-return edge") {
      val ccs = new CopyConstantPropagation("Return")
      import ccs._

      val fAssignmentVals = getAssignmentVals(inMain = false)
      val returnNodeVals = getReturnVals(inMain = true)
      assertResult(fAssignmentVals, "The assigned value in main should be propagated to the return node in f")(returnNodeVals)
//      ccs.printResult(withNullInstructions = false)
    }
  }
}
