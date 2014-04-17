package ca.uwaterloo.dataflow.ide.taint

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TaintAnalysisSpecByNames extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       val taint = new TaintAnalysisSpecBuilder("LocalVars")
       import taint._

       variable("x", "main") shouldBe secret
     }

     it("propagates non-secret values intra-procedurally") {
       val taint = new TaintAnalysisSpecBuilder("NotSecretLocalVars")
       import taint._

       variable("x", "main") shouldBe notSecret
       variable("y", "main") shouldBe notSecret
       variable("z", "main") shouldBe secret
     }

     it("propagates secret values along the call-start edge") {
       val taint = new TaintAnalysisSpecBuilder("FunctionCall")
       import taint._

       variable("s", "f") shouldBe secret
     }

     it("sets a function parameter to top, if that function is invoked with secret and non-secret arguments") {
       val taint = new TaintAnalysisSpecBuilder("MultipleFunctionCalls")
       import taint._

       variable("s", "f") shouldBe secret
     }

     it("propagates secret values along the end-return edge") {
       val taint = new TaintAnalysisSpecBuilder("ReturnSecret")
       import taint._

       variable("s", "main") shouldBe secret
     }

     it("propagates secret-value-storing variables along the end-return edge") {
       val taint = new TaintAnalysisSpecBuilder("Return")
       import taint._

       variable("s", "main") shouldBe secret
     }

     it("assigns top to variables that have been assigned secret and non-secret values in if branches") {
       val taint = new TaintAnalysisSpecBuilder("Phi")
       import taint._

       variable("s", "main") shouldBe secret
     }

     it("propagate constant variables that have been assigned the same value in different if branches") {
       val taint = new TaintAnalysisSpecBuilder("PhiSame")
       import taint._

       variable("s", "main") shouldBe secret
     }
   }
 }
