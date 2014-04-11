package ca.uwaterloo.ide.taint

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IfdsTaintAnalysisSpec extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       val ccs = new TaintAnalysisSpecBuilder("LocalVars")

       variable("t", "main") shouldBe secret
     }

     it("propagates non-secret values intra-procedurally") {
       val ccs = new TaintAnalysisSpecBuilder("NotSecretLocalVars")

       variable("x", "main") shouldBe notSecret
       variable("y", "main") shouldBe notSecret
       variable("z", "main") shouldBe secret
     }

     it("propagates secret values along the call-start edge") {
       val ccs = new TaintAnalysisSpecBuilder("FunctionCall")

       variable("s", "f") shouldBe secret
     }

     it("sets a function parameter to top, if that function is invoked with secret and non-secret arguments") {
       val ccs = new TaintAnalysisSpecBuilder("MultipleFunctionCalls")

       variable("s", "f") shouldBe secret
     }

     it("propagates secret values along the end-return edge") {
       val ccs = new TaintAnalysisSpecBuilder("ReturnSecret")

       variable("s", "main") shouldBe secret
     }

     it("propagates secret-value-storing variables along the end-return edge") {
       val ccs = new TaintAnalysisSpecBuilder("Return")

       variable("s", "main") shouldBe secret
     }

     it("assigns top to variables that have been assigned secret and non-secret values in if branches") {
       val ccs = new TaintAnalysisSpecBuilder("Phi")

       variable("s", "main") shouldBe secret
     }

     it("propagate constant variables that have been assigned the same value in different if branches") {
       val ccs = new TaintAnalysisSpecBuilder("PhiSame")

       variable("t", "main") shouldBe secret
     }
   }
 }
