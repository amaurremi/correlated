package ca.uwaterloo.dataflow.correlated.collector.util

import ca.uwaterloo.dataflow.correlated.collector.CorrelatedCallStats
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import java.io.File

trait RunUtil {

  def getCcStats(testName: String, resourcePath: String = "ca/uwaterloo/dataflow/benchmarks/dacapo/"): CorrelatedCallStats = {
    val config =
      ConfigFactory.load(
        resourcePath + testName,
        ConfigParseOptions.defaults().setAllowMissing(false),
        ConfigResolveOptions.defaults()
      )
    val pa = FlexibleCallGraphBuilder()(config)

    CorrelatedCallStats(pa.cg)
//    CorrelatedCallStats(pa.cgRta) // enable in order to compute analysis using RTA call graph construction
  }

  def runBenchmarks(runner: String => Unit, dir: String) {
    val userDir = System.getProperty("user.dir")
    val benchmarkDir = new File(userDir, dir)
    benchmarkDir.listFiles foreach {
      file =>
        val name = file.getName
        if (name endsWith "jar") {
          println(s"Running $name benchmark...\n")
          runner(name.substring(0, name.lastIndexOf('.')))
        }
    }
  }
}
