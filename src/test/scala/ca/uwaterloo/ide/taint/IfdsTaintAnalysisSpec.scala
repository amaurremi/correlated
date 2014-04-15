package ca.uwaterloo.ide.taint

import ca.uwaterloo.id.ifds.analysis.taint.IfdsTaintAnalysis
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import ca.uwaterloo.id.ifds.solver.VariableFactAnalysisBuilder
import ca.uwaterloo.id.ide.PropagationSpecBuilder

@RunWith(classOf[JUnitRunner])
class IfdsTaintAnalysisSpec extends FunSpec {

   describe("TaintAnalysis") {
     it("propagates secret values intra-procedurally") {
       val ifds = new IfdsTaintAnalysis("LocalVars") with VariableFactAnalysisBuilder with PropagationSpecBuilder
       import ifds._

       variable("x", "main") shouldBe Bottom
     }
   }
 }
