package ca.uwaterloo.dataflow.correlated.collector.util

import ca.uwaterloo.dataflow.correlated.collector.CorrelatedCallStats
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import java.io.File

trait RunUtil {

  def getCcStats(testName: String): CorrelatedCallStats = {
    val resourcePath = "ca/uwaterloo/dataflow/benchmarks/dacapo/"
    val config =
      ConfigFactory.load(
        resourcePath + testName,
        ConfigParseOptions.defaults().setAllowMissing(false),
        ConfigResolveOptions.defaults()
      )
    val pa = FlexibleCallGraphBuilder()(config)

    CorrelatedCallStats(pa.cg)
  }

  def runBenchmarks(runner: String => Unit) {
    val dir = System.getProperty("user.dir")
    val benchmarkDir = new File(dir, "src/test/scala/ca/uwaterloo/dataflow/benchmarks/dacapo/sources")
    benchmarkDir.listFiles foreach {
      file =>
        val name = file.getName
        runner(name.substring(0, name.lastIndexOf('.')))
    }
  }
}
