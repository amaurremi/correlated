package ca.uwaterloo.dataflow.ide.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsProblem
import ca.uwaterloo.dataflow.ide.PropagationSpecBuilder
import ca.uwaterloo.dataflow.ide.analysis.solver.IdeSolver
import ca.uwaterloo.dataflow.ifds.conversion.IdeFromIfdsBuilder
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis

sealed abstract class AbstractTaintAnalysisSpecBuilder (
  fileName: String
) extends IfdsTaintAnalysis(fileName) with VariableFacts with IdeSolver with PropagationSpecBuilder

class TaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisSpecBuilder(fileName) with IdeFromIfdsBuilder {

  override val assertionMap: Map[String, LatticeElem] =
    Map("shouldBeSecret" -> Bottom, "shouldNotBeSecret" -> Top)
}

class CcTaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisSpecBuilder(fileName) with CorrelatedCallsProblem {

  override val assertionMap: Map[String, LatticeElem] =
    Map("shouldNotBeSecretCC" -> Top)
}