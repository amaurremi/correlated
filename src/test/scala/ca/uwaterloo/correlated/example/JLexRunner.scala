package ca.uwaterloo.correlated.example

import ca.uwaterloo.correlated.util.TestUtil

object JLexRunner {

  def main(args: Array[String]) {
    val jlexCcs = TestUtil.getCcs()
    jlexCcs.printInfo()
  }
}
