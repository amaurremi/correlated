package ca.uwaterloo.ide.taint

import ca.uwaterloo.ide.analysis.taint.TaintAnalysis
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TaintAnalysisSpec extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       val ccs = new TaintAnalysis("LocalVars").solvedResult
     }
   }
 }
