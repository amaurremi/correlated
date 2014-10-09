package ca.uwaterloo.dataflow.ide.correlated.analysis

import ca.uwaterloo.dataflow.common.{VariableFacts, WalaInstructions}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsProblemBuilder
import ca.uwaterloo.dataflow.correlated.collector.{Receiver, ReceiverI}
import com.ibm.wala.classLoader.{IClass, IMethod}
import com.ibm.wala.dataflow.IFDS.ISupergraph
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.propagation.{InstanceKey, PointerAnalysis}
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsProblemBuilderSpec extends FunSpec with MockitoSugar {

  describe("CorrelatedFunction") {

    val ccProblemBuilder = new CorrelatedCallsProblemBuilder with WalaInstructions with VariableFacts {
      override type FactElem = VariableFact

      lazy val notNeeded = throw new UnsupportedOperationException("Implementation not needed for this test")
      override lazy val entryPoints: Seq[NodeType] = notNeeded
      override lazy val supergraph: ISupergraph[Node, Procedure] = notNeeded
      override def getValNum(factElem: FactElem, node: XNode): ValueNumber = notNeeded
      override def otherSuccEdges: IdeOtherEdgeFn = notNeeded
      override def otherSuccEdgesPhi: IdeOtherEdgeFn = notNeeded
      override def endReturnEdges: IdeEdgeFn = notNeeded
      override def callReturnEdges: IdeEdgeFn = notNeeded
      override def callStartEdges: IdeEdgeFn = notNeeded
      override lazy val callGraph: CallGraph = notNeeded

      override val pointerAnalysis: PointerAnalysis[InstanceKey] = mock[PointerAnalysis[InstanceKey]]

      lazy val method1 = mock[IMethod]
      lazy val method2 = mock[IMethod]
      lazy val method3 = mock[IMethod]
      lazy val receiver1 = mock[Receiver]
      lazy val receiver2 = mock[Receiver]
      lazy val receiver3 = mock[Receiver]
      lazy val receiver4 = mock[Receiver]

      override def getCcReceivers: Set[ReceiverI] = {
        assert(receiver1 != null)
        Set(receiver1, receiver2, receiver3, receiver4)
      }
    }

    lazy val class1  = mock[IClass]
    lazy val class2  = mock[IClass]
    lazy val class3  = mock[IClass]
    lazy val class4  = mock[IClass]

    describe("compose") {
      import ccProblemBuilder._

      it("identity and top functions") {
        val ct = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val f  = CorrelatedFunction(Map(receiver1 -> ct))
//
        assertResult(Id)(Id ◦ Id)
        assertResult(λTop)(Id ◦ λTop)
        assertResult(f)(Id ◦ f)
        assertResult(f)(f ◦ Id)
        assertResult(CorrelatedFunction(mapReceivers(composedTypesTop) + (receiver1 -> ComposedTypes(TypesTop, ct.unionSet))))(f ◦ λTop)
        assertResult(λTop)(λTop ◦ f)
        assertResult(λTop)(λTop ◦ λTop)
        assertResult(λTop)(λTop ◦ Id)
      }

      it("functions with a different domain") {
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = CorrelatedFunction(Map(receiver1 -> ct1))
        val f2  = CorrelatedFunction(Map(receiver2 -> ct2))

        assertResult(CorrelatedFunction(Map(receiver1 -> ct1, receiver2 -> ct2)))(f1 ◦ f2)
      }

      it("functions with the same domain") {
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = CorrelatedFunction(Map(receiver1 -> ct1))
        val f2  = CorrelatedFunction(Map(receiver1 -> ct2))

        val intersectTypes = ComposedTypes(TypesTop, SetType(Set(class2)))
        assertResult(CorrelatedFunction(Map(receiver1 -> intersectTypes)))(f1 ◦ f2)
      }
    }

    describe("meet") {
      import ccProblemBuilder._

      it("identity and top functions") {
        val ct = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val f  = CorrelatedFunction(Map(receiver1 -> ct))

        assertResult(Id)(Id ⊓ Id)
        assertResult(Id)(Id ⊓ λTop)
        assertResult(CorrelatedFunction(Map(receiver1 -> ComposedTypes(TypesBottom, SetType(Set(class2))))))(Id ⊓ f)
        assertResult(Id ⊓ f)(f ⊓ Id)
        assertResult(f)(f ⊓ λTop)
        assertResult(Id)(λTop ⊓ Id)
        assertResult(λTop)(λTop ⊓ λTop)
        assertResult(f)(λTop ⊓ f)
      }

      it("functions with a different domain") {
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = CorrelatedFunction(Map(receiver1 -> ct1))
        val f2  = CorrelatedFunction(Map(receiver2 -> ct2))

        val union1: ComposedTypes = ComposedTypes(TypesBottom, ct1.unionSet)
        val union2: ComposedTypes = ComposedTypes(TypesBottom, ct2.unionSet)
        assertResult(CorrelatedFunction(Map(receiver1 -> union1, receiver2 -> union2)))(f1 ⊓ f2)
      }

      it("functions with the same domain") {
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = CorrelatedFunction(Map(receiver1 -> ct1))
        val f2  = CorrelatedFunction(Map(receiver1 -> ct2))

        val unionTypes = ComposedTypes(SetType(Set(class1, class3)), SetType(Set(class2, class4)))
        assertResult(CorrelatedFunction(Map(receiver1 -> unionTypes)))(f1 ⊓ f2)
      }
    }
  }
}
