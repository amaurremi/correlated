package ca.uwaterloo.ide

// p. 149 of Sagiv, Reps, Horwitz, "Precise interprocedural dataflow analysis
// with applications to constant propagation"
class ComputeValues[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V],
  jumpFunc: JumpFn[T, V]
) {

  import Util._
  import problem._
  import explodedGraphInfo._

  private[this] lazy val vals: Values[T] = {
    // [1]
    val tops = mutableMap(explodedGraphIterator map {
      _ -> ⊤
    })
    // [2]
    val bottoms = mutableMap(seedNodes(initialSeeds, getSupergraph) map {
      _ -> ⊥
    })
    tops ++ bottoms
  }

  private[this] lazy val nodeWorklist = new NodeWorklist[T] // todo represent in same way as other sets and maps

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
        propagateValue(e.target, edgeFnMap(e)(vals(e.source)))
    }
  }

  private[this] def computeStartNode(p: P) {
    for {
      c <- getCallIdeNodes(p)
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
      nodeWorklist insert n
    }
  }
}
