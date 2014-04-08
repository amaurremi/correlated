package ca.uwaterloo.ide.cp

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {
    it("propagates constants intra-procedurally") {
      val ccs = new CopyConstantPropagationSpecBuilder("LocalVars")
      import ccs._

      val assignmentVals = getVarsAtAssignments(inMain = true)
      val returnNodeVals = getVarsAtReturn(inMain = true)
      assertResult(assignmentVals, "The assigned value should be propagated to the return node")(returnNodeVals)
    }

    it("propagates constants along the call-start edge") {
      val ccs = new CopyConstantPropagationSpecBuilder("FunctionCall")
      import ccs._

      val assignmentVals = getVarsAtAssignments(inMain = true) map onlyLatticeElem
      val returnNodeVals = getVarsAtReturn(inMain = false) map onlyLatticeElem
      assertResult(assignmentVals, "The assigned value in main should be propagated to the return node in f")(returnNodeVals)
    }

    it("sets its parameter to bottom, if same function is invoked with different constant arguments") {
      val ccs = new CopyConstantPropagationSpecBuilder("MultipleFunctionCalls")
      import ccs._

      val returnNodeVals = getVarsAtReturn(inMain = false, expectedNumber = 3)
      val definedInF = returnNodeVals collect {
        case (Variable(method, _), le) if entryPoints forall { e => e.getMethod != method } =>
          le
      }
      assertResult(definedInF)(Seq(⊥))
    }

    it("propagates constants along the end-return edge") {
      val ccs = new CopyConstantPropagationSpecBuilder("ReturnConstant")
      import ccs._

      val returnNodeVals = getVarsAtReturn(inMain = true) map onlyLatticeElem
      val (vn, method) = getReturnVal
      assertResult(Seq(Num(vn, method)), "The value returned in f should be propagated to main")(returnNodeVals)
    }

    it("propagates variable constants along the end-return edge") {
      val ccs = new CopyConstantPropagationSpecBuilder("Return")
      import ccs._

      val fAssignmentVals = getVarsAtAssignments(inMain = false) map onlyLatticeElem
      val returnNodeVals  = getVarsAtReturn(inMain = true) map onlyLatticeElem
      assertResult(fAssignmentVals, "The constant-variable value returned in f should be propagated to main")(returnNodeVals)
    }

    it("assigns bottom to variables that have been assigned different values in if branches") {
      val ccs = new CopyConstantPropagationSpecBuilder("Phi")
      import ccs._

      val returnNodeVals = getVarsAtReturn(inMain = true) map onlyLatticeElem
      assertResult(returnNodeVals, "Variable has been assigned different values in if branches and should be mapped to bottom")(Seq(⊥))
    }

    it("propagate constant variables that have been assigned the same value in different if branches") {
      val ccs = new CopyConstantPropagationSpecBuilder("PhiSame")
      import ccs._

      val returnNodeVal = getVarsAtReturn(inMain = true) map onlyLatticeElem
      val assignmentVals = (getVarsAtAssignments(inMain = true, expectedNumber = 2) map onlyLatticeElem).toSeq.distinct
      assert(assignmentVals.size == 1, "There should be only one distinct assigned value")
      assertResult(assignmentVals, "Variable should be a constant since it has been assigned the same value in different if branches")(returnNodeVal)
    }
  }
}
