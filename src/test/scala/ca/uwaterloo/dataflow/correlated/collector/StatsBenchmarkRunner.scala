package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil

object StatsBenchmarkRunner extends RunUtil {

  def main(args: Array[String]) {
    val runner = (name: String) => getAppCcStats(name).printCommaSeparated()
    runBenchmarks(runner, "src/test/scala/ca/uwaterloo/dataflow/benchmarks/dacapo/sources")
  }
}
