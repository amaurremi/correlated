package ca.uwaterloo.correlated

import com.typesafe.config.ConfigFactory
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsSpec extends FunSpec {

  describe("Correlated Calls") {
      val config = ConfigFactory.load()
      val pa = FlexibleCallGraphBuilder()(config)

      val ccs: CorrelatedCalls = CorrelatedCalls(pa.cg)
      println(ccs.allCallSites)
  }
}
