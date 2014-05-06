package ca.uwaterloo.dataflow.ide.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ide.PropagationSpecBuilder
import ca.uwaterloo.dataflow.ide.analysis.solver.IdeSolver
import ca.uwaterloo.dataflow.ifds.conversion.IdeFromIfdsBuilder
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis

sealed abstract class AbstractTaintAnalysisSpecBuilder (
  fileName: String
) extends IfdsTaintAnalysis(fileName) with VariableFacts with IdeSolver with PropagationSpecBuilder {

  final val secret    = Bottom
  final val notSecret = Top
}

class TaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisSpecBuilder(fileName) with IdeFromIfdsBuilder {

  override def assertionMap: Map[String, LatticeElem] =
    Map("shouldBeSecret" -> secret, "shouldNotBeSecret" -> notSecret)
}

class CcTaintAnalysisSpecBuilder(
  fileName: String
) extends AbstractTaintAnalysisSpecBuilder(fileName) with IdeFromIfdsBuilder {

  override def assertionMap: Map[String, LatticeElem] =
    Map("shouldNotBeSecretCC" -> notSecret)
}