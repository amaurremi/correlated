package ca.uwaterloo.dataflow.ide.taint

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TaintAnalysisSpecByFunctions extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       new TaintAnalysisSpecBuilder("LocalVars").assertSecretValues()
     }

     it("propagates non-secret values intra-procedurally") {
       new TaintAnalysisSpecBuilder("NotSecretLocalVars").assertSecretValues()
     }

     it("propagates secret values along the call-start edge") {
       new TaintAnalysisSpecBuilder("FunctionCall").assertSecretValues()
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

     it("propagate constant variables that have been assigned the same value in different if branches") {
       new TaintAnalysisSpecBuilder("PhiSame").assertSecretValues()
     }
   }
 }
