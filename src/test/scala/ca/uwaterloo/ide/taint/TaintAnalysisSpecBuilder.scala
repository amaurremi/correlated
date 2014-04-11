package ca.uwaterloo.id.ide.taint

import ca.uwaterloo.id.ide.PropagationSpecBuilder
import ca.uwaterloo.id.ide.analysis.taint.TaintAnalysis

class TaintAnalysisSpecBuilder(fileName: String) extends TaintAnalysis(fileName) with PropagationSpecBuilder {

  final val secret    = ⊥

  final val notSecret = ⊤
}