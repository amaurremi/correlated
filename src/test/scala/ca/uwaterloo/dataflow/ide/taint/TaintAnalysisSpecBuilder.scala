package ca.uwaterloo.dataflow.ide.taint

import ca.uwaterloo.dataflow.ide.{IdeSolver, PropagationSpecBuilder}
import ca.uwaterloo.dataflow.ifds.analysis.taint.IfdsTaintAnalysis
import ca.uwaterloo.dataflow.ifds.conversion.IdeFromIfdsBuilder
import ca.uwaterloo.dataflow.common.VariableFacts

class TaintAnalysisSpecBuilder(
  fileName: String
) extends IfdsTaintAnalysis(fileName) with IdeFromIfdsBuilder with VariableFacts with IdeSolver with PropagationSpecBuilder {

  final val secret    = Bottom
  final val notSecret = Top
}