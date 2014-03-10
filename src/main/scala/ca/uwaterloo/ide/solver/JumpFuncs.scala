package ca.uwaterloo.ide

import com.ibm.wala.util.collections.HashSetMultiMap
import scala.collection.JavaConverters._
import scala.collection.{breakOut, mutable}

// p. 147 of Sagiv, Reps, Horwitz, "Precise interprocedural dataflow analysis
// with applications to constant propagation"
trait JumpFuncs { this: IdeProblem with TraverseGraph =>

  private[this] val pathWorklist = new mutable.Queue[IdeEdge]

  private[this] val jumpFn = mutable.Map[IdeEdge, IdeFunction]() withDefault { _ => λTop } // for some reason, withDefalutValue doesn't work

  private[this] val summaryFn = mutable.Map[IdeEdge, IdeFunction]() withDefault { _ => λTop }

  def initialize() {
    // [5]
    val edges = entryPoints map {
      ep =>
        val zeroNode = IdeNode(ep, Λ)
        IdeEdge(zeroNode, zeroNode)
    }
    pathWorklist enqueue (edges: _*)
    // [1-2 + 6]
    jumpFn ++= (edges map {
      _ -> Id
    })(breakOut)
    // [3-4]
  }

  /**
   * Maps (c, sp, d1) to EdgeFn(d4, f)
   * [21]
   */
  private[this] val forwardExitD4s = new HashSetMultiMap[(Node, Node, Fact), Fact]

  /**
   * Maps (sq, c, d4) to (d3, jumpFn) if JumpFn(sq, d3 -> c, d4) != Top
   * [28]
   */
  private[this] val forwardExitD3s = new HashSetMultiMap[(Node, Node, Fact), (Fact, IdeFunction)]

  def computeJumpFuncs: Map[IdeEdge, IdeFunction] = {
    initialize()
    while (pathWorklist.size > 0) {
      val e = pathWorklist.dequeue()
      val f = jumpFn(e)
      val n = e.target
      if (n.isCallNode)
        forwardCallNode(e, f)
      if (n.isExitNode)
        forwardExitNode(n, f)
      if (!n.isCallNode && !n.isExitNode)
        forwardAnyNode(e, f)
    }
    jumpFn.toMap
  }

  /**
   * p. 147, [11-18]
   */
  private[this] def forwardCallNode(e: IdeEdge, f: IdeFunction) {
    val n = e.target
    // [12-13]
    val node = n.n
    for {
      sq <- targetStartNodes(node)
      d3 <- callStartD2s(n, sq)
    } {
      forwardExitFromCall(n, f, sq, d3)
      val sqn = IdeNode(sq, d3)
      propagate(IdeEdge(sqn, sqn), Id)
    }
    // [14-16]
    for {
      r                       <- returnNodes(node)
      FactFunPair(d3, edgeFn) <- callReturnEdges(n, r)
      rn                       = IdeNode(r, d3)
      re                       = IdeEdge(e.source, rn)
    } {
      propagate(re, edgeFn ◦ f)
      // [17-18]
      val f3 = summaryFn(IdeEdge(n, rn))
      if (f3 != λTop)
        propagate(re, f3 ◦ f)
    }
  }

  private[this] def forwardExitNode(n: IdeNode, f: IdeFunction) {
    for {
      (c, r)                <- callReturnPairs(n.n)
      d4                    <- forwardExitD4s.get(c, n.n, n.d).asScala
      FactFunPair(`d4`, f4) <- callStartEdges(IdeNode(c, d4), n.n)
      FactFunPair(d5, f5)   <- endReturnEdges(n, r)
      rn                     = IdeNode(r, d5)
      sumEdge                = IdeEdge(IdeNode(c, d4), rn)
      sumF                   = summaryFn(sumEdge)
      fPrime                 = (f5 ◦ f ◦ f4) ⊓ sumF
      if fPrime != sumF
    } {
      // [26]
      summaryFn += sumEdge -> fPrime
      // [29]
      forwardExitPropagate(c, d4, rn, fPrime)
    }
  }


  private[this] def forwardExitPropagate(c: Node, d4: Fact, rn: IdeNode, fPrime: IdeFunction) {
    for {
      sq <- startNodes(c)
      (d3, f3) <- forwardExitD3s.get(sq, c, d4).asScala
    } {
      // [29]
      propagate(IdeEdge(IdeNode(sq, d3), rn), fPrime ◦ f3)
    }
  }

  private[this] def forwardExitFromCall(n: IdeNode, f: IdeFunction, sq: Node, d: Fact) {
    forwardExitD4s.put((n.n, sq, d), n.d)
    forwardExitNode(n, f)
  }

  private[this] def forwardAnyNode(e: IdeEdge, f: IdeFunction) {
    val n = e.target
    for {
      m                       <- followingNodes(n.n)
      FactFunPair(d3, edgeFn) <- otherSuccEdges(n, m)
    } {
      propagate(IdeEdge(e.source, IdeNode(m, d3)), edgeFn ◦ f)
    }
  }

  private[this] def propagate(e: IdeEdge, f: IdeFunction) {
    val jf = jumpFn(e)
    val f2 = f ⊓ jf
    if (f2 != jf) {
      jumpFn += e -> f2
      if (f2 != λTop)
        forwardExitD3s.put((e.source.n, e.target.n, e.target.d), (e.source.d, f2))
      pathWorklist enqueue e
    }
  }
}
