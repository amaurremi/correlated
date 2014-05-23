package ca.uwaterloo.dataflow.ide.analysis.solver

import ca.uwaterloo.dataflow.common.TraverseGraph
import ca.uwaterloo.dataflow.ide.analysis.problem.IdeProblem
import com.ibm.wala.util.collections.HashSetMultiMap
import scala.collection.JavaConverters._
import scala.collection.{breakOut, mutable}

// p. 147 of Sagiv, Reps, Horwitz, "Precise interprocedural dataflow instance
// with applications to constant propagation"
trait JumpFuncs { this: IdeProblem with TraverseGraph =>

  private[this] val pathWorklist = new mutable.Queue[XEdge]

  // [1-2]
  private[this] val jumpFn = mutable.Map[XEdge, IdeFunction]() withDefault { _ => λTop } // for some reason, withDefalutValue doesn't work

  // [3-4]
  private[this] val summaryFn = mutable.Map[XEdge, IdeFunction]() withDefault { _ => λTop }

  def initialize() {
    // [5]
    val edges = entryPoints map {
      ep =>
        val zeroNode = XNode(ep, Λ)
        XEdge(zeroNode, zeroNode)
    }
    pathWorklist enqueue (edges: _*)
    // [6]
    jumpFn ++= (edges map {
      _ -> Id
    })(breakOut)
  }

  /**
   * Maps (c, sp, d1) to EdgeFn(d4, f)
   * [21]
   */
  private[this] val forwardExitD4s = new HashSetMultiMap[(Node, XNode), Fact]

  /**
   * Maps (sq, c, d4) to (d3, jumpFn) if JumpFn(sq, d3 -> c, d4) != Top
   * [28]
   */
  private[this] val forwardExitD3s = new HashSetMultiMap[(Node, XNode), (Fact, IdeFunction)]

  def computeJumpFuncs: Map[XEdge, IdeFunction] = {
    initialize()
    // [7-33]
    while (pathWorklist.size > 0) {
      // [8-9] e = (sp, d1) -> (n, d2)
      val e = pathWorklist.dequeue()
      val f = jumpFn(e)
      val n = e.target
      if (n.isCallNode)
        forwardCallNode(e, f)
      if (n.isExitNode)
        forwardExitNode(e, f)
      if (!n.isCallNode && !n.isExitNode)
        forwardAnyNode(e, f)
    }
    jumpFn.toMap
  }

  /**
   * p. 147, [11-18]
   */
  private[this] def forwardCallNode(e: XEdge, f: IdeFunction) {
    val n = e.target
    val partialPropagate = propagate(e, f) _
    // [12-13]
    val node = n.n
    for {
      sq <- targetStartNodes(node)
      d3 <- callStartD2s(n, sq)
    } {
      forwardExitFromCall(e, f, sq, d3)
      val sqn = XNode(sq, d3)
      partialPropagate(XEdge(sqn, sqn), Id)
    }
    // [14-16]
    for {
      r                       <- returnNodes(node)
      FactFunPair(d3, edgeFn) <- callReturnEdges(n, r)
      rn                       = XNode(r, d3)
      re                       = XEdge(e.source, rn)
    } {
      partialPropagate(re, edgeFn ◦ f)
      // [17-18]
      val f3 = summaryFn(XEdge(n, rn))
      if (f3 != λTop)
        partialPropagate(re, f3 ◦ f)
    }
  }

  private[this] def forwardExitNode(e: XEdge, f: IdeFunction) {
    val sp = e.source
    val n = e.target
    val node = n.n
    for {
      (c, r)               <- callReturnPairs(node)
      d4                   <- forwardExitD4s.get(c, sp).asScala
      FactFunPair(d1, f4)  <- callStartEdges(XNode(c, d4), sp.n)
      if sp.d == d1
      FactFunPair(d5, f5)  <- endReturnEdges(n, r)
      rn                    = XNode(r, d5)
      sumEdge               = XEdge(XNode(c, d4), rn)
      sumF                  = summaryFn(sumEdge)
      fPrime                = (f5 ◦ f ◦ f4) ⊓ sumF
      if fPrime != sumF
    } {
      // [26]
      summaryFn += sumEdge -> fPrime
      // [29]
      forwardExitPropagate(e, f)(c, d4, rn, fPrime)
    }
  }

  private[this] def forwardExitPropagate(
    e: XEdge,
    f: IdeFunction
  )(
    c: Node,
    d4: Fact,
    rn: XNode,
    fPrime: IdeFunction
  ) {
    for {
      sq <- startNodes(c)
      (d3, f3) <- forwardExitD3s.get(sq, XNode(c, d4)).asScala
      if f3 != λTop
    } {
      // [29]
      propagate(e, f)(XEdge(XNode(sq, d3), rn), fPrime ◦ f3)
    }
  }

  /**
   * To get d4 values in line [21], we need to remember all tuples (c, d4, sp) when we encounter them
   * in the call-processing procedure.
   */
  private[this] def forwardExitFromCall(e: XEdge, f: IdeFunction, sq: Node, d: Fact) {
    val n = e.target
    forwardExitD4s.put((n.n, XNode(sq, d)), n.d)
    if (n.isExitNode) forwardExitNode(e, f)
  }

  /**
   * For line [28], we need to retrieve all d3 values that match the condition. When we encounter
   * them here, we store them in the forwardExitD3s map.
   */
  private[this] def forwardExitFromPropagate(e: XEdge, f2: IdeFunction, oldE: XEdge, oldF: IdeFunction) {
    forwardExitD3s.put((e.source.n, e.target), (e.source.d, f2))
    if (oldE.target.isExitNode) forwardExitNode(oldE, oldF)
  }

  private[this] def forwardAnyNode(e: XEdge, f: IdeFunction) {
    val n = e.target
    for {
      m                       <- followingNodes(n.n).toSeq
      FactFunPair(d3, edgeFn) <- otherSuccEdges(n, m)
    } {
      propagate(e, f)(XEdge(e.source, XNode(m, d3)), edgeFn ◦ f)
    }
  }

  /**
   * @param oldE IDE edge for this propagation. Needed for repeating forwardExitNode
   * @param oldF Original IDE edge function for this propagation. Needed for repeating forwardExitNode
   */
  private[this] def propagate(oldE: XEdge, oldF: IdeFunction)(e: XEdge, f: IdeFunction) {
    val jf = jumpFn(e)
    val f2 = f ⊓ jf
    if (f2 != jf) {
      jumpFn += e -> f2
      if (f2 != λTop) forwardExitFromPropagate(e, f2, oldE, oldF)
      pathWorklist enqueue e
    }
  }
}
