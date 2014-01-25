package ca.uwaterloo.correlated.example

import ca.uwaterloo.correlated.util.TestUtil

object JLexRunner {

  def main(args: Array[String]) {
    println("Flexible call graph (pointer analysis):\n")
    val jlexCcsPa = TestUtil.getCcsForPointerAnalysisCallGraph()
    jlexCcsPa.printInfo()

    println("\n0-CFA call graph:\n")
    val jlexCcs0Cfa = TestUtil.getCcsForZeroCfa("JLex")
    jlexCcs0Cfa.printInfo()
  }
}
