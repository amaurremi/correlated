package ca.uwaterloo.dataflow.correlated.collector.util

import ca.uwaterloo.dataflow.correlated.collector.{AppCorrelatedCallStats, CorrelatedCallStats}
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import edu.illinois.wala.ipa.callgraph.DynamicCallGraphBuilder
import java.io.File

trait RunUtil {

  def getCcStats(
    bmCollectionName: String,
    bmName: String,
    rta: Boolean = false,
    onlyApp: Boolean = false
  ): CorrelatedCallStats = {
    val pa = FlexibleCallGraphBuilder()(createConfig(configPath(bmCollectionName, bmName)))
    val cg = if (rta) pa.cgRta else pa.cg
    if (onlyApp) AppCorrelatedCallStats(cg) else CorrelatedCallStats(cg)
  }

  private[this] def createConfig(configPath: String) =
    ConfigFactory.load(
      configPath,
      ConfigParseOptions.defaults().setAllowMissing(false),
      ConfigResolveOptions.defaults()
    )

  def getDynamicCcStats(
    bmCollectionName: String,
    bmName: String,
    args: Array[String]
  ): CorrelatedCallStats = {
    val cg = new DynamicCallGraphBuilder(createConfig(configPath(bmCollectionName, bmName)), args).cg
    println(s"Computing correlated call stats for $bmName...")
    CorrelatedCallStats(cg)
  }

  /**
   * runner(bmCollectionName: String, bmName: String)
   */
  def runBenchmarks(runner: (String, String) => Unit, bmCollectionName: String) {
    new File(bmPath(bmCollectionName)).listFiles foreach {
      file =>
        val name = file.getName
        if (name endsWith "jar") {
          runner(bmCollectionName, name.substring(0, name.lastIndexOf('.')))
        }
    }
  }

  def bmPath(bmCollectionName: String) =
    System.getProperty("user.dir") + "/src/test/scala/ca/uwaterloo/dataflow/benchmarks/" + bmCollectionName

  def configPath(bmCollectionName: String, bmName: String) =
    "ca/uwaterloo/dataflow/benchmarks/" + bmCollectionName + "/" + bmName
}
