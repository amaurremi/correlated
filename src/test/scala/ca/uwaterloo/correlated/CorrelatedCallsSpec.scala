package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.CallGraphUtil
import com.ibm.wala.ipa.callgraph.CallGraph
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import java.io.File

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsSpec extends FunSpec {

  describe("Correlated Calls") {
    val config = ConfigFactory.load()
    val jarKey = "wala.dependencies.jar"
    val exclusionKey = "wala.exclussions"
    val appJar = if (config.hasPath(jarKey))
      config.getString(jarKey)
    else
      throw new RuntimeException("No Jar dependency to analyze. Please add a dependencies.jar field to the configuration file.")
    val exclusionFile = if (config.hasPath(exclusionKey))
      Some(new File(config.getString(exclusionKey)))
    else None

    val cg: CallGraph = CallGraphUtil.buildZeroCfaCg(appJar, exclusionFile)
    CorrelatedCalls(cg).printInfo()
  }
}
