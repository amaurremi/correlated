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
        new NormalTaintAnalysisRunner(dir, name).printResultSize()
        new CcTaintAnalysisRunner(dir, name).printResultSize()
      }
    runBenchmarks(runner)
  }

  abstract class AbstractTaintAnalysisRunner(
    dirName: String,
    fileName: String
  ) extends IfdsTaintAnalysis(dirName + fileName)
  with VariableFacts
  with AbstractIdeToIfds
  with SecretInput {

    def printResultSize()
  }

  class NormalTaintAnalysisRunner(
    dirName: String,
    fileName: String
  ) extends AbstractTaintAnalysisRunner(dirName, fileName)
    with IdeFromIfdsBuilder
    with IdeToIfds {

    override def printResultSize() {
      printf("%s IFDS result: %d\n", fileName, ifdsResult.size)
    }
  }

  class CcTaintAnalysisRunner(
    dirName: String,
    fileName: String
  ) extends AbstractTaintAnalysisRunner(dirName, fileName)
    with CorrelatedCallsToIfds
    with CcReceivers {

    override def printResultSize() {
      printf("%s CC result: %d\n", fileName, ifdsResult.size)
    }
  }
}
