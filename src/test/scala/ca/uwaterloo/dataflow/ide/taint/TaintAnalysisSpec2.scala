package ca.uwaterloo.dataflow.ide.taint

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TaintAnalysisSpec2 extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       new TaintAnalysisSpecBuilder("LocalVars").assertSecretValues()
     }
   }
 }
