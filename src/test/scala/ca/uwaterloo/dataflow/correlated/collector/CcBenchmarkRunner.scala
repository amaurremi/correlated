package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.common.{Time, AbstractIdeToIfds, VariableFacts}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsToIfds
import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil
import ca.uwaterloo.dataflow.ifds.conversion.{IdeToIfds, IdeFromIfdsBuilder}
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis
import ca.uwaterloo.dataflow.ifds.instance.taint.impl.{CcReceivers, SecretInput}
import org.scalatest.FunSpec
import Time.time

object CcBenchmarkRunner extends FunSpec with RunUtil {

  def main(args: Array[String]): Unit = {
    runSingleBm("specjvm", "db")
  }

  def runSpecJvm(): Unit = {
    run("specjvm")
  }

  def runDacapo(): Unit = {
    run("dacapo")
  }

  def runOther(): Unit = {
    run("other")
  }

  def runSingleBm(bmCollectionName: String, bmName: String): Unit = {
    println(s"Running $bmName benchmark...")
    val path: String = configPath(bmCollectionName, bmName)
//    new NormalTaintAnalysisRunner(path, bmName).printResultSize()
    val runner = time("Preparing analysis") { new CcTaintAnalysisRunner(path, bmName) }
    runner.printResultSize()
    println()
  }

  def run(bmCollectionName: String): Unit = {
    val runner =
      (bmCollectionName: String, bmName: String) =>
        runSingleBm(bmCollectionName, bmName)
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
      val result = time ("Computing correlated-calls result") { ifdsResult }
      printf("%s CC result: %d\n", bmName, result.size)
    }
  }
}
