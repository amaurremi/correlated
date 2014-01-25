package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.TestUtil._
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsSpec extends FunSpec {

  describe("Correlated Calls") {

    describe("NoCcs") {
      val ccs = getCcsForZeroCfa("NoCcs")
      ccs.printInfo()
    }
  }
}
