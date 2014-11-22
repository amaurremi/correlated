package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil

object StatsBenchmarkRunner extends App with RunUtil {

  runSpecJvm()

  def runDacapo() {
    run("dacapo")
  }

  def runNonJava() {
    run("nonJava")
  }

  def runSpecJvm() {
    run("specjvm")
  }

  def run(bmCollectionName: String) {
    val runner =
      (bmCollectionName: String, bmName: String) =>
        getCcStats(bmCollectionName, bmName, rta = false, onlyApp = false).printCommaSeparated()
    runBenchmarks(runner, bmCollectionName)
  }
}
