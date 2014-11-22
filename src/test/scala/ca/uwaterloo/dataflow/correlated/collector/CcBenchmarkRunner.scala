package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.common.{AbstractIdeToIfds, VariableFacts}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsToIfds
import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil
import ca.uwaterloo.dataflow.ifds.conversion.{IdeToIfds, IdeFromIfdsBuilder}
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis
import ca.uwaterloo.dataflow.ifds.instance.taint.impl.{CcReceivers, SecretInput}
import org.scalatest.FunSpec

object CcBenchmarkRunner extends FunSpec with RunUtil {

  def main(args: Array[String]): Unit = {
    runSpecJvm()
  }

  def runSpecJvm(): Unit = {
    run("specjvm")
  }

  def runDacapo(): Unit = {
    run("dacapo")
  }

  def run(bmCollectionName: String): Unit = {
    val runner =
      (bmCollectionName: String, bmName: String) => {
        val path: String = configPath(bmCollectionName, bmName)
        new NormalTaintAnalysisRunner(path, bmName).printResultSize()
        new CcTaintAnalysisRunner(path, bmName).printResultSize()
      }
    runBenchmarks(runner, bmCollectionName)
  }

  abstract class AbstractTaintAnalysisRunner(
    configPath: String
  ) extends IfdsTaintAnalysis(configPath)
  with VariableFacts
  with AbstractIdeToIfds
  with SecretInput {

    def printResultSize()
  }

  class NormalTaintAnalysisRunner(
    configPath: String,
    bmName: String
  ) extends AbstractTaintAnalysisRunner(configPath)
    with IdeFromIfdsBuilder
    with IdeToIfds {

    override def printResultSize() {
      printf("%s IFDS result: %d\n", bmName, ifdsResult.size)
    }
  }

  class CcTaintAnalysisRunner(
    configPath: String,
    bmName: String
  ) extends AbstractTaintAnalysisRunner(configPath)
    with CorrelatedCallsToIfds
    with CcReceivers {

    override def printResultSize() {
      printf("%s CC result: %d\n", bmName, ifdsResult.size)
    }
  }
}
