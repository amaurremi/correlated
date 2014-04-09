package ca.uwaterloo.ide.taint

import ca.uwaterloo.ide.PropagationSpecBuilder
import ca.uwaterloo.ide.analysis.taint.TaintAnalysis

class TaintAnalysisSpecBuilder(fileName: String) extends TaintAnalysis(fileName) with PropagationSpecBuilder {

  final val secret    = ⊥

  final val notSecret = ⊤
}