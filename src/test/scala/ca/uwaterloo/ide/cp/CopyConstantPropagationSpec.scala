package ca.uwaterloo.id.ide.cp

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {

    it("propagates constants intra-procedurally") {
      val ccs = new CopyConstantPropagationSpecBuilder("LocalVars")
      import ccs._

      shouldBeAConstant(variable("x", "main"))
    }

    it("propagates constants along the call-start edge") {
      val ccs = new CopyConstantPropagationSpecBuilder("FunctionCall")
      import ccs._

      shouldBeAConstant(variable("s", "f"))
    }

    it("sets its parameter to bottom, if same function is invoked with different constant arguments") {
      val ccs = new CopyConstantPropagationSpecBuilder("MultipleFunctionCalls")
      import ccs._

      shouldNotBeAConstant(variable("s", "f"))
    }

    it("propagates constants along the end-return edge") {
      val ccs = new CopyConstantPropagationSpecBuilder("ReturnConstant")
      import ccs._

      shouldBeAConstant(variable("x", "main"))
    }

    it("propagates variable constants along the end-return edge") {
      val ccs = new CopyConstantPropagationSpecBuilder("Return")
      import ccs._

      shouldBeAConstant(variable("x", "main"))
    }

    it("assigns bottom to variables that have been assigned different values in if branches") {
      val ccs = new CopyConstantPropagationSpecBuilder("Phi")
      import ccs._

      shouldNotBeAConstant(variable("x", "main"))
    }

    it("propagate constant variables that have been assigned the same value in different if branches") {
      val ccs = new CopyConstantPropagationSpecBuilder("PhiSame")
      import ccs._

      shouldBeAConstant(variable("x", "main"))
    }
  }
}
