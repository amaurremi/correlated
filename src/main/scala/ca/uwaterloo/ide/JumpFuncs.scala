package ca.uwaterloo.ide

import scala.collection.JavaConverters._
import scala.collection.mutable

class JumpFuncs[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V]
) {

  import Util.mutableMap
  import problem._
  import supergraphInfo._

  private[this] val pathWorklist = new PathWorklist(problem.initialSeeds)

  private[this] val jumpFn: JumpFn[T, V] = {
    val initialSeeds = problem.initialSeeds().iterator().asScala
    val jumpFn = mutableMap(initialSeeds map {
      seed =>
        (IdeEdge(seed), Top)
    })
    ??? // todo: p. 147, line 6
  }

  private[this] val summaryFn: mutable.Map[IdeEdge[T], V] =
    mutableMap(
      callReturnEdges map {
        _ -> Top
      })

  def compute: JumpFn[T, V] = {
    while (pathWorklist.size > 0) {
      val e = pathWorklist.take()
      val f = jumpFn(e)
      val n = e.target
      n match {
        case CallNode(_, _)    => forwardCallNode(n, f)
        case en@ExitNode(_, _) => forwardExitNode(en, e, f)
        case _                 => forwardProcNode(e, f)
      }
    }
    jumpFn
  }

  private[this] def forwardExitNode(en: ExitNode[T], e: IdeEdge[T], f: V) {
    callReturnEdges(en.n) map {
      case cre@IdeEdge(c, r) =>
        val f4   = edgeFn(IdeEdge(c, e.source))
        val f5   = edgeFn(IdeEdge(en, r))
        val sumF = summaryFn(cre)
        val f6   = (f5 ◦ f ◦ f4) ⊓ sumF
        if (f6 != sumF) {
          summaryFn += cre -> f6
          startNodes(c.n) map {
            sq =>
              val f3 = jumpFn(IdeEdge(sq, c))
              if (f3 != Top)
                propagate(IdeEdge(sq, r), f6 ◦ f3)
          }
        }
    }
  }

  private[this] def forwardCallNode(n: IdeNode[T], f: V) {
    edgesWithSource(n) collect {
      case IdeEdge(_, callee@CallNode(_, _)) =>
        propagate(IdeEdge(callee, callee), Id)
      case e@IdeEdge(_, r@ReturnNode(_, _)) =>
        propagate(IdeEdge(e.source, r), edgeFn(e) ◦ f)
        val sumF = summaryFn(e)
        if (sumF != Top)
          propagate(e, sumF ◦ f)
    }
  }

  private[this] def forwardProcNode(e: IdeEdge[T], f: V) {
    edgesWithSource(e.target) map {
      edge =>
        propagate(IdeEdge(e.source, edge.target), edgeFn(edge) ◦ f)
    }
  }

  private[this] def propagate(e: IdeEdge[T], f: V) {
    val jumpFn = jumpFn(e)
    val f2     = f ⊓ jumpFn
    if (f2 != jumpFn) {
      jumpFn += e -> f2
      pathWorklist.insert(e)
    }
  }
}
