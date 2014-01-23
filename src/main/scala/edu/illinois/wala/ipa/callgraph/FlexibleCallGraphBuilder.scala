package edu.illinois.wala.ipa.callgraph

import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph._
import com.ibm.wala.ipa.callgraph.propagation._
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ssa.{DefaultIRFactory, IRFactory}
import com.typesafe.config.{Config, ConfigFactory}

object FlexibleCallGraphBuilder {
  def apply(entrypoint: (String, String), dependencies: Iterable[Dependency])(implicit config: Config): AbstractCallGraphBuilder =
    apply(AnalysisOptions(Seq(entrypoint), dependencies))

  def apply(entrypoint: (String, String), dependency: String)(implicit config: Config = ConfigFactory.load): AbstractCallGraphBuilder =
    apply(entrypoint, Seq(Dependency(dependency)))
    
  def apply()(implicit config: Config): AbstractCallGraphBuilder = 
    apply(AnalysisOptions())

  def apply(options: AnalysisOptions): AbstractCallGraphBuilder = 
    new FlexibleCallGraphBuilder(options)
}

class FlexibleCallGraphBuilder(
  val _cha: ClassHierarchy,
  val _options: AnalysisOptions,
  val _cache: AnalysisCache, pointerKeys: PointerKeyFactory
) extends SSAPropagationCallGraphBuilder(_cha, _options, _cache, pointerKeys)
  with AbstractCallGraphBuilder
  with ExtraFeatures {

  def this(
    cha: ClassHierarchy,
    options: AnalysisOptions,
    irFactory: IRFactory[IMethod]
  ) = this(cha, options, new AnalysisCache(irFactory), new DefaultPointerKeyFactory())

  def this(options: AnalysisOptions) = this(options.cha, options, new DefaultIRFactory())

  final lazy val heap = getPointerAnalysis.getHeapGraph

  setContextInterpreter(theContextInterpreter)
  setContextSelector(cs)
  setInstanceKeys(instanceKeys)

  val cg = makeCallGraph(options)
  val cache = _cache

  implicit val implicitCha = cha
}
