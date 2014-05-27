package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.common.{AbstractIdeToIfds, VariableFacts}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsToIfds
import ca.uwaterloo.dataflow.correlated.collector.util.TestUtil
import ca.uwaterloo.dataflow.ifds.conversion.{IdeToIfds, IdeFromIfdsBuilder}
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis
import ca.uwaterloo.dataflow.ifds.instance.taint.impl.{CcReceivers, SecretInput}

object CcBenchmarkRunner extends TestUtil {

  def main(args: Array[String]) {
    val runner =
      (name: String) => {
        val dir = "ca/uwaterloo/dataflow/benchmarks/dacapo/"
        printResultSize(new NormalTaintAnalysisRunner(dir + name))
        printResultSize(new CcTaintAnalysisRunner(dir + name))
      }
    runBenchmarks(runner)
  }

  private[this] def printResultSize(analysis: AbstractTaintAnalysisRunner) {
    val result = analysis.ifdsResult
    println("Normal IFDS result size: " + result.size)
  }

  abstract class AbstractTaintAnalysisRunner(
    fileName: String
  ) extends IfdsTaintAnalysis(fileName)
  with VariableFacts
  with AbstractIdeToIfds
  with SecretInput

  class NormalTaintAnalysisRunner(
    fileName: String
  ) extends AbstractTaintAnalysisRunner(fileName)
    with IdeFromIfdsBuilder
    with IdeToIfds

  class CcTaintAnalysisRunner(
    fileName: String
  ) extends AbstractTaintAnalysisRunner(fileName)
    with CorrelatedCallsToIfds
    with CcReceivers
}