package ca.uwaterloo.dataflow.ide.taint

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TaintAnalysisSpecByFunctions extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       val taint = new TaintAnalysisSpecBuilder("LocalVars")
       taint.assertSecretValues()
     }

     it("propagates non-secret values intra-procedurally") {
       new TaintAnalysisSpecBuilder("NotSecretLocalVars").assertSecretValues()
     }

     it("propagates secret values along the call-start edge (static method)") {
       new TaintAnalysisSpecBuilder("FunctionCall").assertSecretValues()
     }

     it("propagates secret values along the call-start edge (instance method)") {
       new TaintAnalysisSpecBuilder("FunctionCall2").assertSecretValues()
     }

     it("propagates secret values along the call-start edge (instance method, multiple parameters)") {
       new TaintAnalysisSpecBuilder("FunctionCall3").assertSecretValues()
     }

     it("propagates secret values along the call-start edge (instance method, multiple parameters, multiple files)") {
       new TaintAnalysisSpecBuilder("MultipleFiles").assertSecretValues()
     }

     it("sets a function parameter to top, if that function is invoked with secret and non-secret arguments") {
       new TaintAnalysisSpecBuilder("MultipleFunctionCalls").assertSecretValues()
     }

     it("propagates secret values along the end-return edge") {
       new TaintAnalysisSpecBuilder("ReturnSecret").assertSecretValues()
     }

     it("propagates secret-value-storing variables along the end-return edge") {
       new TaintAnalysisSpecBuilder("Return").assertSecretValues()
     }

     it("assigns top to variables that have been assigned secret and non-secret values in if branches") {
       new TaintAnalysisSpecBuilder("Phi").assertSecretValues()
     }

     it("switch statement") {
       new TaintAnalysisSpecBuilder("Switch").assertSecretValues()
     }

     it("switch statement 2") {
       new TaintAnalysisSpecBuilder("Switch2").assertSecretValues()
     }

     it("switch statement 3") {
       new TaintAnalysisSpecBuilder("Switch3").assertSecretValues()
     }

     it("if statement") {
       new TaintAnalysisSpecBuilder("If").assertSecretValues()
     }

     it("if statement 2") {
       new TaintAnalysisSpecBuilder("If2").assertSecretValues()
     }

     it("if statement 3") {
       new TaintAnalysisSpecBuilder("If3").assertSecretValues()
     }

     it("if statement 4") {
       new TaintAnalysisSpecBuilder("If4").assertSecretValues()
     }

     it("nested if statement") {
       new TaintAnalysisSpecBuilder("NestedIf").assertSecretValues()
     }

     it("nested if statement 2") {
       new TaintAnalysisSpecBuilder("NestedIf2").assertSecretValues()
     }

     it("ternary operator") {
       new TaintAnalysisSpecBuilder("Ternary").assertSecretValues()
     }

     it("string operations") {
       new TaintAnalysisSpecBuilder("StringOps").assertSecretValues()
     }

     it("propagate constant variables that have been assigned the same value in different if branches") {
       new TaintAnalysisSpecBuilder("PhiSame").assertSecretValues()
     }
   }
 }
