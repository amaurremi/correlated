package ca.uwaterloo.id.ide.taint

import ca.uwaterloo.id.ide.PropagationSpecBuilder
import ca.uwaterloo.id.ifds.analysis.taint.IfdsTaintAnalysis
import ca.uwaterloo.id.ifds.solver.VariableFactAnalysisBuilder

class TaintAnalysisSpecBuilder(fileName: String) extends IfdsTaintAnalysis(fileName) with VariableFactAnalysisBuilder with PropagationSpecBuilder {

  final val secret    = Bottom

  final val notSecret = Top
}