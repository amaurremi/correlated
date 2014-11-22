package ca.uwaterloo.dataflow.correlated.collector.util

import ca.uwaterloo.dataflow.correlated.collector.{AppCorrelatedCallStats, CorrelatedCallStats}
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import java.io.File

trait RunUtil {

  def getCcStats(
    testName: String,
    resourcePath: String,
    rta: Boolean = false,
    onlyApp: Boolean = false
  ): CorrelatedCallStats = {
    val pa = createPA(testName, resourcePath)
    val cg = if (rta) pa.cgRta else pa.cg
    if (onlyApp) AppCorrelatedCallStats(cg) else CorrelatedCallStats(cg)
  }

  private[this] def createPA(
    testName: String,
    resourcePath: String
  ) = {
    val config =
      ConfigFactory.load(
        resourcePath + (if (resourcePath endsWith "/") testName else "/" + testName),
        ConfigParseOptions.defaults().setAllowMissing(false),
        ConfigResolveOptions.defaults()
      )
    FlexibleCallGraphBuilder()(config)
  }

  def runBenchmarks(runner: String => Unit, dir: String) {
    val userDir = System.getProperty("user.dir")
    val benchmarkDir = new File(userDir, "src/test/scala/" + dir)
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
