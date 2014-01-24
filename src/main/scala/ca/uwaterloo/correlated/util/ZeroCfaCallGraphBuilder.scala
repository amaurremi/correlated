package ca.uwaterloo.correlated.util

import com.ibm.wala.ipa.callgraph._
import com.ibm.wala.ipa.callgraph.impl.Util.{makeMainEntrypoints, makeZeroCFABuilder}
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.util.config.AnalysisScopeReader
import java.io.File

object ZeroCfaCallGraphBuilder {

  def apply(appJar: String, exclusionFile: Option[File]): CallGraph = {
    val scope: AnalysisScope =
      AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, exclusionFile getOrElse null)
    val cha = ClassHierarchy.make(scope)
    val entrypoints = makeMainEntrypoints(scope, cha)
    val options: AnalysisOptions = new AnalysisOptions(scope, entrypoints)
    val builder: CallGraphBuilder = makeZeroCFABuilder(options, new AnalysisCache, cha, scope)
    builder.makeCallGraph(options, null)
  }
}
