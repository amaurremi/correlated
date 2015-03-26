package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.common.Time.time
import ca.uwaterloo.dataflow.common.{AbstractIdeToIfds, Time, VariableFacts}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsToIfds
import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil
import ca.uwaterloo.dataflow.ifds.conversion.{IdeFromIfdsBuilder, IdeToIfds}
import ca.uwaterloo.dataflow.ifds.instance.taint.IfdsTaintAnalysis
import ca.uwaterloo.dataflow.ifds.instance.taint.impl.{CcReceivers, SecretInput}
import org.scalatest.FunSpec

trait BenchmarkRunner extends FunSpec with RunUtil {

  def main(args: Array[String], equivAnalysis: Boolean): Unit =
    if (args.length <= 0)
      println("No benchmarks specified.")
    else if (args.length == 1)
      run(args(0))
    else {
      val bm = args(0)
      for {
        i <- 1 until args.length
      } {
        runSingleBm(bm, args(i), equivAnalysis)
      }
    }

  def runSingleBm(bmCollectionName: String, bmName: String, equivAnalysis: Boolean = false): Unit = {
    println(s"Running $bmName benchmark...")
    val path: String = configPath(bmCollectionName, bmName)
    val runner = time("Preparing analysis") {
      if (equivAnalysis) new NormalTaintAnalysisRunner(path, bmName)
      else new CcTaintAnalysisRunner(path, bmName)
    }
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

    def printResultSize(equiv: Boolean, bmName: String) = {
      val (analysis, algorithm) = if (equiv) ("equivalence", "IFDS") else ("CC", "IDE")
      val result = ifdsResult
      printf("%s %s result: %d\n", bmName, algorithm, result.size)
      val supNodes = supergraph.getNumberOfNodes
      val ifdsNodes = ifdsResult.keySet.size
      val explNodes = ifdsResult.values.flatten.size
      println("Number of supergraph nodes: " + supNodes)
      println("Number of supergraph nodes in IFDS result: " + ifdsNodes)
      println("Number of exploded nodes: " + explNodes)
    }

    def printResultSize()
  }

  class NormalTaintAnalysisRunner(
    configPath: String,
    bmName: String
  ) extends AbstractTaintAnalysisRunner(configPath)
    with IdeFromIfdsBuilder
    with IdeToIfds {

    override def printResultSize() {
      printResultSize(equiv = true, bmName)
    }
  }


  class CcTaintAnalysisRunner(
    configPath: String,
    bmName: String
  ) extends AbstractTaintAnalysisRunner(configPath)
    with CorrelatedCallsToIfds
    with CcReceivers {

    override def printResultSize() {
      printResultSize(equiv = false, bmName)
    }
  }
}
