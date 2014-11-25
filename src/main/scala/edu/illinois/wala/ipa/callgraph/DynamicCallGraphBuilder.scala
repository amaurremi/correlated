package edu.illinois.wala.ipa.callgraph

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil
import com.ibm.wala.core.tests.shrike.DynamicCallGraphTestBase
import com.ibm.wala.core.tests.util.TestConstants
import com.ibm.wala.ipa.callgraph._
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.typesafe.config.Config
import org.junit.Assert

class DynamicCallGraphBuilder(config: Config, args: Array[String]) extends DynamicCallGraphTestBase {

  private[this] val javaIO = System.getProperty("java.io.tmpdir")
  private[this] val instrumentedJarLocation = javaIO + File.separator + "test.jar"
  private[this] val cgLocation = javaIO + File.separator + "cg.txt"

  def cg: CallGraph = {
    val testJar = config getStringList "wala.dependencies.jar" get 0
    val rtJar = config getString "wala.jre-lib-path"
    val testMain = config getString "wala.entry.class" substring 1
    instrument(testJar)
    runProgram(testMain.replace('/', '.'), args)
    staticCG("L" + testMain)
  }

  private[this] def getClasspathEntry(elt: String): Option[String] = {
    (System getProperty "java.class.path" split File.pathSeparator).toSeq collectFirst {
      case s if (s indexOf elt) >= 0 =>
        val e: File = new File(s)
        Assert.assertTrue(elt + " expected to exist", e.exists)
        if (e.isDirectory && !s.endsWith("/")) s + "/" else s
    }
  }

  private[this] def runProgram(mainClass: String, args: Array[String]) {
    val shrikeBin = getClasspathEntry("com.ibm.wala.shrike")
    val utilBin = getClasspathEntry("com.ibm.wala.util")
    (shrikeBin, utilBin) match {
      case (Some(sb), Some(ub)) =>
        val jcl = new URLClassLoader(Array[URL](
          new URL("file://" + instrumentedJarLocation),
          new URL ("file://" + shrikeBin),
          new URL("file://" + utilBin)))
        val testClass = jcl.loadClass(mainClass)
        Assert.assertNotNull(testClass)
        val testMain = testClass.getDeclaredMethod("main", classOf[Array[String]])
        Assert.assertNotNull(testMain)
        System.setProperty("dynamicCGFile", cgLocation)
        System.setProperty("dynamicCGHandleMissing", "true")
        testMain.invoke(null, args)
        val runtimeClass: Class[_] = jcl.loadClass("com.ibm.wala.shrike.cg.Runtime")
        Assert.assertNotNull(runtimeClass)
        val endTrace: Method = runtimeClass.getDeclaredMethod("endTrace")
        Assert.assertNotNull(endTrace)
        endTrace.invoke(null)
        Assert.assertTrue("expected to create call graph", new File(System.getProperty("dynamicCGFile")).exists)
      case _ =>
        Assert.assertFalse("can't find shrike or util paths", false)
    }
  }

  private[this] def staticCG(testMain: String): CallGraph = {
    val scope = CallGraphTestUtil.makeJ2SEAnalysisScope(TestConstants.WALA_TESTDATA, CallGraphTestUtil.REGRESSION_EXCLUSIONS)
    val cha = ClassHierarchy.make(scope)
    val entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(scope, cha, testMain)
    val options = CallGraphTestUtil.makeAnalysisOptions(scope, entrypoints)
    CallGraphTestUtil.buildZeroOneCFA(options, new AnalysisCache, cha, scope, false)
  }
}
