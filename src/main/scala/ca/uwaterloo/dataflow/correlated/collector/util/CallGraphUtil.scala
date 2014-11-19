package ca.uwaterloo.dataflow.correlated.collector.util

import java.io.File

import ca.uwaterloo.dataflow.correlated.collector.util.Converter._
import com.ibm.wala.ipa.callgraph._
import com.ibm.wala.ipa.callgraph.impl.Util.{makeMainEntrypoints, makeZeroCFABuilder}
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.util.config.AnalysisScopeReader
import com.ibm.wala.util.graph.traverse.SCCIterator

object CallGraphUtil {

  def buildZeroCfaCg(appJar: String, exclusionFile: Option[File]): CallGraph = {
    val scope: AnalysisScope =
      AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, exclusionFile.orNull)
    val cha = ClassHierarchy.make(scope)
    val entryPoints = makeMainEntrypoints(scope, cha)
    val options: AnalysisOptions = new AnalysisOptions(scope, entryPoints)
    val builder: CallGraphBuilder = makeZeroCFABuilder(options, new AnalysisCache, cha, scope)
    builder.makeCallGraph(options, null)
  }

  def getRcs(cg: CallGraph): List[Set[CGNode]] = {
    val rcs = toScalaList(new SCCIterator[CGNode](cg)) map toScalaSet
    rcs filter {
      rc =>
        rc.size > 1 || rc.size == 1 && isRecursive(cg, rc.headOption.get)
    }
  }

  private[this] def isRecursive(callGraph: CallGraph, cgNode: CGNode) =
    callGraph.getPossibleSites(cgNode, cgNode).hasNext
}
