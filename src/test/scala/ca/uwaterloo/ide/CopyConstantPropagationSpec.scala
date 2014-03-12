package ca.uwaterloo.ide.example.cp

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CopyConstantPropagationSpec extends FunSpec {

  describe("CopyConstantPropagation") {
    it("propagates the zero fact") {
      val ccs = new CopyConstantPropagation("LocalVars")
      ccs.printResult()
    }
  }
}
