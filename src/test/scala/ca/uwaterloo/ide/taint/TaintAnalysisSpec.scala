package ca.uwaterloo.ide.taint

import ca.uwaterloo.ide.analysis.PropagationTester
import ca.uwaterloo.ide.analysis.taint.TaintAnalysis
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TaintAnalysisSpec extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       val ccs = new TaintAnalysis("LocalVars") with PropagationTester
       ccs.getVarsAtReturn(inMain = true)
     }

     it("propagates secret values along the call-start edge") {
       val ccs = new TaintAnalysis("FunctionCall") with PropagationTester
       import ccs._

       val assignmentVals = getVarsAtAssignments(inMain = true)
       val returnNodeVals = getVarsAtReturn(inMain = false)
       assertResult(assignmentVals, "The assigned value in main should be propagated to the return node in f")(returnNodeVals)
     }

     it("sets a function parameter to top, if that function is invoked with secret and non-secret arguments") {
       val ccs = new TaintAnalysis("MultipleFunctionCalls") with PropagationTester
       import ccs._

       val returnNodeVals = getVarsAtReturn(inMain = false, expectedNumber = 3)
       val definedInF     = filterByOrigin(returnNodeVals, inMain = false) map onlyLatticeElem
       assertResult(definedInF)(Seq(⊤))
     }

     it("propagates secret values along the end-return edge") {
       val ccs = new TaintAnalysis("ReturnSecret") with PropagationTester
       import ccs._

       val returnNodeVals = getVarsAtReturn(inMain = true)
       val (vn, method)   = getReturnVal
       assertResult(Seq((Variable(method, vn), ⊥)), "The value returned in f should be propagated to main")(returnNodeVals)
     }

     it("propagates secret-value-storing variables along the end-return edge") {
       val ccs = new TaintAnalysis("Return") with PropagationTester
       import ccs._

       val fAssignmentVals = getVarsAtAssignments(inMain = false)
       val returnNodeVals  = getVarsAtReturn(inMain = true)
       assertResult(fAssignmentVals, "The secret-value-storing variable returned in f should be propagated to main")(returnNodeVals)
     }

     it("assigns top to variables that have been assigned secret and non-secret values in if branches") {
       val ccs = new TaintAnalysis("Phi") with PropagationTester
       import ccs._

       val returnNodeVals = getVarsAtReturn(inMain = true) map onlyLatticeElem
       assertResult(returnNodeVals, "Variable has been assigned secret and non-secret values in if branches and should be mapped to bottom")(Seq(⊤))
     }

     it("propagate constant variables that have been assigned the same value in different if branches") {
       val ccs = new TaintAnalysis("PhiSame") with PropagationTester
       import ccs._

       val returnNodeVal = getVarsAtReturn(inMain = true)
       val assignmentVals = getVarsAtAssignments(inMain = true, expectedNumber = 2).toSeq.distinct
       assert(assignmentVals.size == 1, "There should be only one distinct assigned value")
       assertResult(assignmentVals, "Variable should be a secret since it has been assigned only secret values in different if branches")(returnNodeVal)
     }
   }
 }
