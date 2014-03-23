package ca.uwaterloo.ide.example.cp

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {
    it("propagates constants inter-procedurally") {
      val ccs = new CopyConstantPropagation("LocalVars")
      ccs.printResult(withNullInstructions = false)
    }
  }
}
