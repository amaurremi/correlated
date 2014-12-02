package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.RunUtil

object StatsBenchmarkRunner extends App with RunUtil {

  // Run all benchmarks under 'benchmarks/other'
  runOther()

  // Run a specific benchmark
  // runSingleBm("other", "check")
  // runSingleBm("other", "raytrace")

  def runOther() {
    runStatic("other")
  }

  def runDacapo() {
    runStatic("dacapo")
  }

  def runNonJava() {
    runStatic("nonJava")
  }

  def runSpecJvm() {
    runStatic("specjvm")
  }

  def runDynamicAntlr() {
    getDynamicCcStats("dacapo", "antlr", Array[String]("-s", "small", "antlr"))
  }

  def runDynamicJess(): Unit = {
    getDynamicCcStats("specjvm", "jess", Array[String]())
  }

  def runStatic(bmCollectionName: String) {
    val runner =
      (bmCollectionName: String, bmName: String) =>
        runSingleBm(bmCollectionName, bmName)
    runBenchmarks(runner, bmCollectionName)
  }

  def runSingleBm(bmCollectionName: String, bmName: String) {
    val ccStats = getCcStats(bmCollectionName, bmName, rta = false, onlyApp = false)
    assert(ccStats.monomorphicCallSiteNum + ccStats.polymorphicCallSiteNum == ccStats.totalCallSiteNum)
    ccStats.printInfo()
    ccStats.printCorrelated(bmName)
  }
}
