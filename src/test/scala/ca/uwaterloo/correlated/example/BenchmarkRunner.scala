package ca.uwaterloo.correlated.example

import ca.uwaterloo.correlated.util.TestUtil

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
    TestUtil.getCcsForPointerAnalysisCallGraph(testName).printInfo()
  }
}
