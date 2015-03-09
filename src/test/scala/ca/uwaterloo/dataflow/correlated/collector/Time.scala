package ca.uwaterloo.dataflow.correlated.collector

object Time {

  def time[R](block: => R): R = {
    def sec() = System.nanoTime() / 1000000000.0
    val t0 = sec()
    val result = block
    val t1 = sec()
    println("    Elapsed time: " + (t1 - t0) + " s")
    result
  }
}
