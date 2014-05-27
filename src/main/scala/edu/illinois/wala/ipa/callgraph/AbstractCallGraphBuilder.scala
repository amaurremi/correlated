package edu.illinois.wala.ipa.callgraph

import com.ibm.wala.analysis.pointers.HeapGraph
import com.ibm.wala.ipa.callgraph.AnalysisCache
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.ContextSelector
import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector
import com.ibm.wala.ipa.callgraph.impl.Util
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import com.ibm.wala.ipa.cha.ClassHierarchy

trait AbstractCallGraphBuilder {
  def _options: AnalysisOptions
  def _cache: AnalysisCache
  def _cha: ClassHierarchy

  // public
  def heap: HeapGraph
  def cg: CallGraph
  
  // just helpers
  lazy val defaultInterpreter = new DefaultSSAInterpreter(_options, _cache)
//  lazy val reflectionInterpreter = new DelegatingSSAContextInterpreter(
//    ReflectionContextInterpreter.createReflectionContextInterpreter(_cha, _options, _cache), defaultInterpreter)
  Util.addDefaultSelectors(_options, _cha)
  Util.addDefaultBypassLogic(_options, _options.getAnalysisScope, classOf[Util].getClassLoader, _cha)

  // Hooks
  def policy = {
    import ZeroXInstanceKeys._
    ALLOCATIONS }
  protected def cs: ContextSelector = new ContextInsensitiveSelector() // new DefaultContextSelector(_options, _cha)
  protected def contextInterpreter = defaultInterpreter // new DelegatingSSAContextInterpreter(defaultInterpreter, reflectionInterpreter)
  protected def instanceKeys = new ZeroXInstanceKeys(_options, _cha, theContextInterpreter, policy)

  // the rest...
  val theContextInterpreter = contextInterpreter
}