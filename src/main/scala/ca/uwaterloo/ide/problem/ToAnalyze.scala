package ca.uwaterloo.ide

import com.ibm.wala.ipa.callgraph.{AnalysisCache, CallGraph}

trait ToAnalyze {

  val callGraph: CallGraph

  val analysisCache: AnalysisCache
}
