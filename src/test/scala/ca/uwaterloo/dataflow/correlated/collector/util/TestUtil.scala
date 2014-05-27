package ca.uwaterloo.dataflow.correlated.collector.util

import ca.uwaterloo.dataflow.correlated.collector.CorrelatedCallStats
import com.ibm.wala.ipa.callgraph.CallGraph
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import java.io.File

object TestUtil {

  private val resourcePath = "ca/uwaterloo/dataflow/benchmarks/dacapo/"

  def getCcStats(testName: String): CorrelatedCallStats = {
    implicit val config =
      ConfigFactory.load(
        resourcePath + testName,
        ConfigParseOptions.defaults().setAllowMissing(false),
        ConfigResolveOptions.defaults()
      )
    val pa = FlexibleCallGraphBuilder()

    CorrelatedCallStats(pa.cg)
  }
}
