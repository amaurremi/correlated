package ca.uwaterloo.dataflow.ide.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ide.PropagationSpecBuilder
import ca.uwaterloo.dataflow.ide.analysis.solver.IdeSolver
import ca.uwaterloo.dataflow.ifds.conversion.IdeFromIfdsBuilder
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis

class TaintAnalysisSpecBuilder(
  fileName: String
) extends IfdsTaintAnalysis(fileName) with IdeFromIfdsBuilder with VariableFacts with IdeSolver with PropagationSpecBuilder {

  final val secret    = Bottom
  final val notSecret = Top
}