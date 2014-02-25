package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph

// p. 149 of Sagiv, Reps, Horwitz, "Precise interprocedural dataflow analysis
// with applications to constant propagation"
class ComputeValues[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V],
  jumpFunc: JumpFn[T, V]
)(
  implicit supergraph: ISupergraph[T, P]
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
    val bottoms = mutableMap(seedNodes(initialSeeds)(supergraph) map {
      _ -> ⊥
    })
    tops ++ bottoms
  }

  private[this] lazy val nodeWorklist = new NodeWorklist(initialSeeds, zeroFact) // todo represent in same way as other sets and maps

  def compute: Values[T] = {
    // Phase II(i)
    while (!nodeWorklist.isEmpty) {
      val node = nodeWorklist.take()
      if (node.isStartNode)
        computeStartNode(enclProc(node.n))
      if (node.isCallNode)
        computeCallNode(node)
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

  private[this] def computeCallNode(c: IdeNode[T]) {
    val cn = c.n
    val cd = c.d
    for {
      sq                      <- targetStartNodes(cn)
      FactFunPair(dPrime, edgeFn) <- edgeFunctions.callStartFns(cn, cd, sq)
    } yield propagateValue(IdeNode(sq, dPrime), edgeFn(vals(IdeNode(cn, cd))))
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
