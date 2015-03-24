package ca.uwaterloo.dataflow.correlated.collector

object EquivBenchmarkRunner extends BenchmarkRunner {

  def main(args: Array[String]): Unit =
    main(args, equivAnalysis = true)
}
