package ca.uwaterloo.dataflow.correlated.collector

object CcBenchmarkRunner extends BenchmarkRunner {

  def main(args: Array[String]): Unit =
    main(args, equivAnalysis = false)
}
