package ca.uwaterloo.dataflow.correlated.collector.util

import ca.uwaterloo.dataflow.correlated.collector.CorrelatedCalls
import com.ibm.wala.ipa.callgraph.CallGraph
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import java.io.File

object TestUtil {

  private val resourcePath = "correlated/"

  def getCcsForPointerAnalysisCallGraph(testName: String): CorrelatedCalls = {
    implicit val config =
      ConfigFactory.load(
        resourcePath + testName,
        ConfigParseOptions.defaults().setAllowMissing(false),
        ConfigResolveOptions.defaults()
      )
    val pa = FlexibleCallGraphBuilder()

    CorrelatedCalls(pa.cg)
  }

  def getCcsForZeroCfa(testName: String = ""): CorrelatedCalls = {

    def getFileName(paths: String*) = paths mkString "/" replaceAll ("//", "/")

    val config = ConfigFactory.load(resourcePath + "application_zero_cfa")
    val exclusionKey = "wala.exclussions"
    val projectPath = "wala.projectPath"
    val inputProgramPath =
      if (config.hasPath(projectPath))
        config.getString(projectPath)
      else
        throw new RuntimeException("Please specify your absolute project path in the application.config file.")
    val testPath = "src/test/scala/ca/uwaterloo/dataflow/correlated/collector/inputPrograms/"
    val appJar =
      if (testName.isEmpty) {
        val jarKey = "wala.dependencies.jar"
        if (config.hasPath(jarKey))
          config.getString(jarKey)
        else
          throw new RuntimeException(
            "No Jar dependency to analyze for test " + testName +
              ". Please add a dependencies.jar field to the configuration file.")
      } else
        getFileName(inputProgramPath, testPath, testName, testName + ".jar")
    val exclusionFile =
      if (config.hasPath(exclusionKey))
        Some(new File(config.getString(exclusionKey)))
      else None

    val cg: CallGraph = CallGraphUtil.buildZeroCfaCg(appJar, exclusionFile)
    CorrelatedCalls(cg)
  }
}
