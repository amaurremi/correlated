package ca.uwaterloo.dataflow.ide.correlated.analysis

import ca.uwaterloo.dataflow.common.{VariableFacts, WalaInstructions}
import ca.uwaterloo.dataflow.correlated.analysis.CorrelatedCallsProblemBuilder
import com.ibm.wala.classLoader.{IClass, IMethod}
import com.ibm.wala.dataflow.IFDS.ISupergraph
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class CorrelatedCallsProblemBuilderSpec extends FunSpec with MockitoSugar {

  describe("CorrelatedFunction") {

    val ccProblemBuilder = new CorrelatedCallsProblemBuilder with WalaInstructions with VariableFacts {
      lazy val notNeeded = throw new UnsupportedOperationException("Implementation not needed for this test")
      override type FactElem = VariableFact
      override lazy val entryPoints: Seq[Node] = notNeeded
      override lazy val supergraph: ISupergraph[Node, Procedure] = notNeeded
      override def getValNum(factElem: FactElem, node: XNode): ValueNumber = notNeeded
      override def otherSuccEdges: IdeEdgeFn = notNeeded
      override def endReturnEdges: IdeEdgeFn = notNeeded
      override def callReturnEdges: IdeEdgeFn = notNeeded
      override def callStartEdges: IdeEdgeFn = notNeeded
    }

    val vn1 = 3
    val vn2 = 5

    val method1 = mock[IMethod]
    val method2 = mock[IMethod]
    val class1  = mock[IClass]
    val class2  = mock[IClass]
    val class3  = mock[IClass]
    val class4  = mock[IClass]

    describe("compose") {
      import ccProblemBuilder._

      it("identity and top functions") {
        val r  = Receiver(vn1, method1)
        val ct = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val f  = SomeCorrelatedFunction(Map(r -> ct))

        assertResult(Id)(Id ◦ Id)
        assertResult(λTop)(Id ◦ λTop)
        assertResult(f)(Id ◦ f)
        assertResult(f)(f ◦ Id)
        assertResult(λTop)(f ◦ λTop) // todo check that's correct
        intercept[UnsupportedOperationException](λTop ◦ f)
        intercept[UnsupportedOperationException](λTop ◦ λTop)
        intercept[UnsupportedOperationException](λTop ◦ Id)
      }

      it("functions with a different domain") {
        val r1  = Receiver(vn1, method1)
        val r2  = Receiver(vn2, method2)
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = SomeCorrelatedFunction(Map(r1 -> ct1))
        val f2  = SomeCorrelatedFunction(Map(r2 -> ct2))

        assertResult(SomeCorrelatedFunction(Map(r1 -> ct1, r2 -> ct2)))(f1 ◦ f2)
      }

      it("functions with the same domain") {
        val r   = Receiver(vn1, method1)
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = SomeCorrelatedFunction(Map(r -> ct1))
        val f2  = SomeCorrelatedFunction(Map(r -> ct2))

        val intersectTypes = ComposedTypes(TypesTop, SetType(Set(class2)))
        assertResult(SomeCorrelatedFunction(Map(r -> intersectTypes)))(f1 ◦ f2)
      }
    }

    describe("meet") {
      import ccProblemBuilder._

      it("identity and top functions") {
        val r  = Receiver(vn1, method1)
        val ct = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val f  = SomeCorrelatedFunction(Map(r -> ct))

        assertResult(Id)(Id ⊓ Id)
        assertResult(Id)(Id ⊓ λTop)
        val function = SomeCorrelatedFunction(Map(r -> ComposedTypes(TypesBottom, SetType(Set(class2)))))
        val actual: CorrelatedFunction = Id ⊓ f
        assertResult(function)(actual)
        assertResult(Id ⊓ f)(f ⊓ Id)
        assertResult(f)(f ⊓ λTop)
        assertResult(Id)(λTop ⊓ Id)
        assertResult(λTop)(λTop ⊓ λTop)
        assertResult(f)(λTop ⊓ f)
      }

      it("functions with a different domain") {
        val r1  = Receiver(vn1, method1)
        val r2  = Receiver(vn2, method2)
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = SomeCorrelatedFunction(Map(r1 -> ct1))
        val f2  = SomeCorrelatedFunction(Map(r2 -> ct2))

        assertResult(SomeCorrelatedFunction(Map(r1 -> ct1, r2 -> ct2)))(f1 ⊓ f2)
      }

      it("functions with the same domain") {
        val r   = Receiver(vn1, method1)
        val ct1 = ComposedTypes(SetType(Set(class1)), SetType(Set(class2)))
        val ct2 = ComposedTypes(SetType(Set(class3)), SetType(Set(class4)))
        val f1  = SomeCorrelatedFunction(Map(r -> ct1))
        val f2  = SomeCorrelatedFunction(Map(r -> ct2))

        val unionTypes = ComposedTypes(SetType(Set(class1, class3)), SetType(Set(class2, class4)))
        assertResult(SomeCorrelatedFunction(Map(r -> unionTypes)))(f1 ⊓ f2)
      }
    }
  }
}
