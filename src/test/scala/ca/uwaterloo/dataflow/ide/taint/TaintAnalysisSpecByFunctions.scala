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

     it("propagates secret values along the return edge (static method)") {
       new TaintAnalysisSpecBuilder("FunctionReturn").assertSecretValues()
     }

     it("propagates secret values along the return edge (static method) 2") {
       new TaintAnalysisSpecBuilder("FunctionReturn2").assertSecretValues()
     }

     it("propagates secret values along the return edge (static method) 3") {
       new TaintAnalysisSpecBuilder("FunctionReturn3").assertSecretValues()
     }

     it("propagates secret values along the return edge (static method) 4") {
       new TaintAnalysisSpecBuilder("FunctionReturn4").assertSecretValues()
     }

     it("propagates secret values along the return edge (instance method) 5") {
       new TaintAnalysisSpecBuilder("FunctionReturn5").assertSecretValues()
     }

     it("propagates secret values along the return edge (instance method) 6") {
       new TaintAnalysisSpecBuilder("FunctionReturn6").assertSecretValues()
     }

     it("propagates secret values along the return edge (instance method) 7") {
       new TaintAnalysisSpecBuilder("FunctionReturn7").assertSecretValues()
     }

     it("propagates secret values along the return edge (instance method) 8") {
       new TaintAnalysisSpecBuilder("FunctionReturn8").assertSecretValues()
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

     it("propagate constant variables that have been assigned the same value in different if branches") {
       new TaintAnalysisSpecBuilder("PhiSame").assertSecretValues()
     }

// FT: all of the tests below have stack overflow or nontermination problems for me

// stack overflow
     it("for statement") {
       new TaintAnalysisSpecBuilder("For").assertSecretValues()
     }

// nontermination
     it("for statement 2") {
       new TaintAnalysisSpecBuilder("For2").assertSecretValues()
     }

// stack overflow
     it("while statement") {
       new TaintAnalysisSpecBuilder("While").assertSecretValues()
     }

// non-termination
     it("while statement 2") {
       new TaintAnalysisSpecBuilder("While2").assertSecretValues()
     }

// stack overflow
      it("do statement") {
        new TaintAnalysisSpecBuilder("Do").assertSecretValues()
      }

// non-termination
     it("do statement 2") {
       new TaintAnalysisSpecBuilder("Do2").assertSecretValues()
     }

// nontermination
     it("StringBuffer") {
       new TaintAnalysisSpecBuilder("StringBuffer").assertSecretValues()
     }

// nontermination
     it("string concatenation") {
       new TaintAnalysisSpecBuilder("StringConcat").assertSecretValues()
     }

// nontermination
     it("string operations") {
       new TaintAnalysisSpecBuilder("StringOps").assertSecretValues()
     }


   }
 }
