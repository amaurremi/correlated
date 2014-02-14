package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.TabulationSolver
import scala.collection.mutable

class IdeSolver[T, P, F](
  problem: IdeProblem[T, P, F]
) extends TabulationSolver[T, P, F](
  problem,
  null
) {
  import problem.superGraphInfo._

  private[this] val initializeJumpFn = ???

  private[this] val initializeSummaryFn = ???
  
  private[this] val JumpFn: mutable.Map[IdeEdge[T], IdeFunction] = initializeJumpFn

  private[this] val SummaryFn: mutable.Map[IdeEdge[T], IdeFunction] = initializeSummaryFn

  private[this] val PathWorkList = makeWorklist

  def forwardComputeJumpFunctionsSlrps() {
    while (PathWorkList.size > 0) {
      val e = IdeEdge(PathWorkList.take())
      val f = JumpFn(e)
      val n = e.target
      n match {
        case CallNode(_, _)    => forwardCallNode(n, f)
        case en@ExitNode(_, _) => forwardExitNode(en, e, f)
        case ProcNode(_, _)    => forwardProcNode(e, f)
      }
    }
  }

  def forwardExitNode(en: ExitNode[T], e: IdeEdge[T], f: IdeFunction) {
    callReturnEdges(en.n) map {
      case cre@IdeEdge(c, r) =>
        val f4   = edgeFn(IdeEdge(c, e.source))
        val f5   = edgeFn(IdeEdge(en, r))
        val sumF = SummaryFn(cre)
        val f6   = (f5 ◦ f ◦ f4) ⊓ sumF
        if (f6 != sumF) {
          SummaryFn += cre -> f6
          startNodes(c.n) map {
            sq =>
              val f3 = JumpFn(IdeEdge(sq, c))
              propagate(IdeEdge(sq, r), f6 ◦ f3)
          }
        }
    }
  }

  private[this] def forwardCallNode(n: IdeNode[T], f: IdeFunction) {
    edgesWithSource(n) collect {
      case IdeEdge(_, callee@CallNode(_, _)) =>
        propagate(IdeEdge(callee, callee), IdeFunction.Id)
      case e@IdeEdge(_, r@ReturnNode(_, _)) =>
        propagate(IdeEdge(e.source, r), edgeFn(e) ◦ f)
        val sumF = SummaryFn(e)
        if (sumF != IdeFunction.Top)
          propagate(e, sumF ◦ f)
    }
  }

  private[this] def forwardProcNode(e: IdeEdge[T], f: IdeFunction) {
    edgesWithSource(e.target) map {
      edge =>
        propagate(IdeEdge(e.source, edge.target), edgeFn(edge) ◦ f)
    }
  }

  def propagate(e: IdeEdge[T], f: IdeFunction) {
    val jumpFn = JumpFn(e)
    val f2     = f ⊓ jumpFn
    if (f2 != jumpFn) {
      JumpFn += e -> f2
      PathWorkList.insert(e.getWalaPathEdge)
    }
  }
}
