package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil

object StatsBenchmarkRunner extends App with RunUtil {

  runNonJava()

  def runDacapo() {
    run("ca/uwaterloo/dataflow/benchmarks/dacapo")
  }

  def runNonJava() {
    run("ca/uwaterloo/dataflow/benchmarks/nonJava")
  }

  def run(path: String) {
    val runner = (name: String) => getCcStats(name, path, rta = false, onlyApp = false).printCommaSeparated()
    runBenchmarks(runner, path)
  }
}
