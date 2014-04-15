package ca.uwaterloo.id.ide.taint

import ca.uwaterloo.id.ide.{IdeSolver, PropagationSpecBuilder}
import ca.uwaterloo.id.ifds.analysis.taint.IfdsTaintAnalysis
import ca.uwaterloo.id.ifds.conversion.IdeFromIfdsBuilder
import ca.uwaterloo.id.common.VariableFacts

class TaintAnalysisSpecBuilder(
  fileName: String
) extends IfdsTaintAnalysis(fileName) with IdeFromIfdsBuilder with VariableFacts with IdeSolver with PropagationSpecBuilder {

  final val secret    = Bottom
  final val notSecret = Top
}