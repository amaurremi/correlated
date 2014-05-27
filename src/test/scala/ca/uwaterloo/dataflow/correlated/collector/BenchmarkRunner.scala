package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.TestUtil

object BenchmarkRunner {

  def main(args: Array[String]) {
    run("antlr")
    run("bloat")
    run("chart")
    run("eclipse")
    run("fop")
    run("hsqldb")
    run("jython")
    run("luindex")
    run("lusearch")
    run("pmd")
    run("xalan")
  }
  
  private[this] def run(testName: String) {
    println(testName + " benchmark:\n")
    TestUtil.getCcStats(testName).printInfo()
  }
}
