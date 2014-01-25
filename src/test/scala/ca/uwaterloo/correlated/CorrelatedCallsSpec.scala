package ca.uwaterloo.correlated

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import ca.uwaterloo.correlated.util.TestUtil.getCcs

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsSpec extends FunSpec {

  describe("Correlated Calls") {

    describe("NoCcs") {
      val ccs = getCcs("NoCcs")
      ccs.printInfo()
    }
  }
}
