package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.common.{AbstractIdeToIfds, VariableFacts}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsToIfds
import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil
import ca.uwaterloo.dataflow.ifds.conversion.{IdeToIfds, IdeFromIfdsBuilder}
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis
import ca.uwaterloo.dataflow.ifds.instance.taint.impl.{CcReceivers, SecretInput}
import org.scalatest.FunSpec

object CcBenchmarkRunner extends FunSpec with RunUtil {

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
