package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.common.{VariableFacts, WalaInstructions}
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.ssa.SSAInvokeInstruction

trait CorrelatedCallsProblem extends CorrelatedCallsProblemBuilder with WalaInstructions with VariableFacts { this: IfdsProblem =>

  override type FactElem = ValueNumber

  override def otherSuccEdges: IdeEdgeFn  =
    (ideN1, n2) =>
      ifdsOtherSuccEdges(ideN1, n2) flatMap idFactFunPairSet

  override def endReturnEdges: IdeEdgeFn =
    (ideN1, n2) => {
      val d2s = ifdsEndReturnEdges(ideN1, n2)
      val optLocalVar = ideN1.d match {
        case Variable(method, elem) if method == ideN1.n.getMethod =>
          Some(elem)
        case _                                                     =>
          None
      }
      optLocalVar match {
        case Some(localVar) =>
          val maybeLambdaSet = if (ideN1.d == Λ) idFactFunPairSet(Λ) else Set.empty
          val localToBottom  = (d2s - Λ) map {
            FactFunPair(_, SomeCorrelatedFunction(Map(
              Receiver(localVar, ideN1.n.getMethod) -> ComposedTypes(TypesBottom, TypesBottom))
            ))
          }
          maybeLambdaSet ++ localToBottom
        case None           => d2s flatMap idFactFunPairSet
      }
    }

  /**
   * Creates a new fact
   */
  override def callReturnEdges: IdeEdgeFn =
    (ideN1, n2) => {
      val n1 = ideN1.n
      val d1 = ideN1.d
      val d2s = ifdsCallReturnEdges(ideN1, n2)
      if (d1 == Λ) {
        val edgeFn = n1.getLastInstruction match {
          case invokeInstr: SSAInvokeInstruction =>
            callValNum(invokeInstr) map {
              valNum =>
                SomeCorrelatedFunction(Map(
                  Receiver(valNum, n1.getMethod) -> ComposedTypes(TypesBottom, TypesBottom))
                )
            }
        }
        idFactFunPairSet(Λ) ++ (edgeFn match {
          case Some(f) =>
            (d2s - Λ) map { FactFunPair(_, f) }
          case None    =>
            d2s flatMap idFactFunPairSet
        })
      } else d2s flatMap idFactFunPairSet
    }

  override def callStartEdges: IdeEdgeFn  =
    (ideN1, n2) =>
      if (n2.getMethod.isStatic)
        ifdsCallStartEdges(ideN1, n2) flatMap idFactFunPairSet
      else {
        val n1 = ideN1.n
        val edgeFn = n1.getLastInstruction match {
          case invokeInstr: SSAInvokeInstruction =>
            val receiver = Receiver(invokeInstr.getReceiver, enclProc(n1).getMethod)
            val types = staticTypes(n1) // todo check definition
            SomeCorrelatedFunction(Map(receiver -> ComposedTypes(SetType(types), TypesBottom)))
        }
        val d2s = ifdsCallStartEdges(ideN1, n2) - Λ
        val nonLambdaPairs = d2s map {
          FactFunPair(_, edgeFn)
        }
        val maybeLambdaSet = if (ideN1.d == Λ) idFactFunPairSet(Λ) else Set.empty
        nonLambdaPairs ++ maybeLambdaSet
      }

  private[this] def staticTypes(node: Node): Set[IClass] =
    (getCalledNodes(node) map {
      enclProc(_).getMethod.getDeclaringClass
    }).toSet
}
