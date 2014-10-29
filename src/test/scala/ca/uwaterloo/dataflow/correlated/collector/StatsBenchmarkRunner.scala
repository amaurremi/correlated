package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil
import org.scalatest.FunSpec

object StatsBenchmarkRunner extends FunSpec with RunUtil {

  describe("StatsBenchmarkRunner") {
    it("computes stats for benchmarks") {
      val runner = (name: String) => getCcStats(name).printInfo()
      runBenchmarks(runner)
    }
  }
}
