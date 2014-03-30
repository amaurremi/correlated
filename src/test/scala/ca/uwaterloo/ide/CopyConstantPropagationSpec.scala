package ca.uwaterloo.ide.example.cp

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {
//    it("propagates constants intra-procedurally") {
//      val ccs = new CopyConstantPropagation("LocalVars")
//      ccs.printResult(withNullInstructions = false)
//    }

//    it("propagates constants along the call-start edge") {
//      val ccs = new CopyConstantPropagation("FunctionCall")
//      ccs.printResult(withNullInstructions = false)
//    }

    it("propagates constants along the end-return edge") {
      val ccs = new CopyConstantPropagation("Return")
      ccs.printResult(withNullInstructions = false)
    }
  }
}
