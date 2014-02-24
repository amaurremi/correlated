package ca.uwaterloo.ide

import scala.collection.mutable
import com.ibm.wala.util.collections.HashSetMultiMap
import scala.collection.JavaConverters._

// p. 147 of Sagiv, Reps, Horwitz, "Precise interprocedural dataflow analysis
// with applications to constant propagation"
class JumpFuncs[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V]
) {

  import Util.mutableMap
  import problem._
  import explodedGraphInfo._

  private[this] val pathWorklist = new PathWorklist(problem.initialSeeds)

  private[this] val jumpFn: JumpFn[T, V] = {
    val seeds  = Util.getSeeds(problem.initialSeeds)
    // [1-2]
    val jumpFn = mutableMap(intraEdgesFromStart map {
      _ -> Top
    })
    // [6]
    jumpFn ++ mutableMap(seeds map {
        IdeEdge(_, getSupergraph) -> Id // todo:  here, jumpFn != Top, but we can ignore that for forwardExitD4s. right?
    })
  }

  private[this] val summaryFn: mutable.Map[IdeEdge[T], V] =
    mutableMap(
      allCallReturnIdeEdges map {
        _ -> Top
      })

  /**
   * Maps (c, sp, d1) to EdgeFn(d4, f) for line 21
   */
  private[this] val forwardExitD4s = new HashSetMultiMap[(T, T, Fact), Fact]

  /**
   * Maps (sq, c, d4) to d3s if JumpFn(sq, d3 -> c, d4) != Top
   */
  private[this] val forwardExitD3s = new HashSetMultiMap[(T, T, Fact), (Fact, V)]

  def compute: JumpFn[T, V] = {
    while (pathWorklist.size > 0) {
      val e = pathWorklist.take()
      val f = jumpFn(e)
      val n = e.target
      n match {
        case _: CallNode[T]    => forwardCallNode(e, f)
        case en: ExitNode[T]   => forwardExitNode(n, f)
        case _                 => forwardOtherNode(e, f)
      }
    }
    jumpFn
  }

  /**
   * p. 147, [11-18]
   */
  private[this] def forwardCallNode(e: IdeEdge[T], f: V) {
    val n = e.target
    // [12-13]
    val node = n.n
    val d2   = n.d
    for {
      sq <- targetStartNodes(node)
      d3 <- edgeFunctions.callStartD2s(node, d2, sq)
      _   = forwardExitFromCall(n, f, sq, d3) // todo ugly?
      sqn = IdeNode(sq, d3, getSupergraph)
    } yield propagate(IdeEdge(sqn, sqn), Id)
    // [14-16]
    for {
      r                       <- returnNodes(node)
      FactFunPair(d3, edgeFn) <- edgeFunctions.callReturnEdges(node, d2, r)
      rn                       = IdeNode(r, d3, getSupergraph)
      re                       = IdeEdge(e.source, rn)
      _                        = propagate(re, edgeFn ◦ f)
      // [17-18]
      f3                       = summaryFn(IdeEdge(n, rn))
      if f3 != Top
    } yield propagate(re, f3 ◦ f)
  }

  private[this] def forwardExitNode(n: IdeNode[T], f: V) {
    for {
      (c, r)                <- callReturnPairs(n.n)
      d4                    <- forwardExitD4s.get(c, n.n, n.d).asScala
      FactFunPair(`d4`, f4) <- edgeFunctions.callStartFns(c, d4, n.n)
      FactFunPair(d5, f5)   <- edgeFunctions.endReturnEdges(n.n, n.d, r)
      rn                     = IdeNode(r, d5, getSupergraph)
      sumEdge                = IdeEdge(IdeNode(c, d4, getSupergraph), rn)
      sumF                   = summaryFn(sumEdge)
      f$                     = (f5 ◦ f ◦ f4) ⊓ sumF
      if f$ != sumF
      // [26]
      _                     <- summaryFn += sumEdge -> f$
      sq                    <- startNodes(c)
      (d3, f3)              <- forwardExitD3s.get(sq, c, d4).asScala
      // [29]
    } yield propagate(IdeEdge(IdeNode(sq, d3, getSupergraph), rn), f$ ◦ f3)
  }

  private[this] def forwardExitFromCall(n: IdeNode[T], f: V, sq: T, d: Fact) {
    forwardExitD4s.put((n.n, sq, d), n.d)
    forwardExitNode(n, f)
  }

  private[this] def forwardOtherNode(e: IdeEdge[T], f: V) {
    val n = e.target
    for {
      m                       <- followingNodes(n.n)
      FactFunPair(d3, edgeFn) <- edgeFunctions.otherSuccEdges(n.n, n.d, m)
    } yield propagate(IdeEdge(e.source, IdeNode(m, d3, getSupergraph)), edgeFn ◦ f)
  }

  // todo map for line 28 on page 147

  private[this] def propagate(e: IdeEdge[T], f: V) {
    val jf = jumpFn(e)
    val f2 = f ⊓ jf
    if (f2 != jf) {
      jumpFn += e -> f2
      if (f2 != Top)
        forwardExitD3s.put((e.source.n, e.target.n, e.target.d), (e.source.d, f2))
      pathWorklist.insert(e)
    }
  }
}
