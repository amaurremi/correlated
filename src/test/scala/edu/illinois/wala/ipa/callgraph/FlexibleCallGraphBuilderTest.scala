package edu.illinois.wala.ipa.callgraph

import com.typesafe.config.ConfigFactory
import org.junit.Assert
import org.junit.Test

class FlexibleCallGraphBuilderTest {
  @Test def testRegexEntrypoints() {
    val b = FlexibleCallGraphBuilder()(ConfigFactory.load().withFallback(
      ConfigFactory.parseString("wala.entry.signature-pattern = \".*createEntry\\\\(ILjava/lang/Object;Ljava/lang/Object;I\\\\)V.*\"")))
    Assert.assertTrue(b.cg.getMaxNumber > 20)
  }
}