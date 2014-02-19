package ca.uwaterloo.ide

import scala.collection.mutable

class ComputeValues[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V],
  jumpFunc: JumpFn[T, V]
) {
  import problem._
  import supergraphInfo._
  import Util.mutableMap

  private[this] lazy val vals: Values[T] =
    mutableMap(supergraphIterator.map {
      node =>
        (IdeNode(node, ???), ⊤)
    })

  private[this] lazy val nodeWorklist = new NodeWorklist[T]

  def compute: Values[T] = {
    // Phase II(i)
    while (!nodeWorklist.isEmpty) {
      nodeWorklist.take().n match {
        case StartNode(sn: T, _) => computeStartNode(enclProc(sn))
        case CallNode(cn: T, _)  => computeCallNode(cn)
      }
    }
    // Phase II(ii)
    for {
      n <- notCallOrStartNodes
      e <- edgesWithTarget(n)
      f  = jumpFunc(e)
      if f != Top
      t  = e.target
    } yield
      vals += t -> (vals(t) ⊓ f(vals(e.source)))
    vals
  }

  private[this] def computeCallNode(cn: T) {
    callStartEdges(cn) map {
      e =>
        propagateValue(e.target, edgeFn(e)(vals(e.source)))
    }
  }

  private[this] def computeStartNode(p: P) {
    for {
      c <- getCallNodes(p)
      e <- edgesWithTarget(c)
      f2 = jumpFunc(e)
      if f2 != Top
    } yield
      propagateValue(e.target, f2(vals(e.source)))
  }

  private[this] def propagateValue(n: IdeNode[T], v: LatticeNum) {
    val ln = vals(n)
    val v2 = v ⊓ ln
    if (v2 != ln) {
      vals += n -> v2
      nodeWorklist.insert(n)
    }
  }

  private[this] def edgesWithTarget(n: T): Seq[IdeEdge[T]] = ??? // todo IS EASY
}
