package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.TestUtil

object StatsBenchmarkRunner extends TestUtil {

  def main(args: Array[String]) {
    val runner = (name: String) => getCcStats(name).printInfo()
    runBenchmarks(runner)
  }
}
