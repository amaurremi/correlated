package ca.uwaterloo.ide

import scala.collection.{breakOut, mutable}

// p. 149 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
// with applications to constant propagation"
trait ComputeValues { this: IdeProblem with TraverseGraph =>

  /**
   * [1]
   */
  private[this] val vals = mutable.Map[IdeNode, LatticeElem]() withDefault { _ => Top }

  private[this] lazy val nodeWorklist = mutable.Queue[IdeNode]()

  private[this] def initialize() {
    // [3]
    nodeWorklist ++= entryPoints map { IdeNode(_, Λ) }
    // [2]
    vals ++= (entryPoints map {
      IdeNode(_, Λ) -> Bottom
    })(breakOut)
  }

  def computeValues(jumpFunc: Map[IdeEdge, IdeFunction]): Map[IdeNode, LatticeElem]  = {
    initialize()
    // Phase II(i)
    while (!nodeWorklist.isEmpty) {
      val node = nodeWorklist.dequeue()
      if (node.isStartNode)
        computeStartNode(node, jumpFunc)
      if (node.isCallNode)
        computeCallNode(node)
    }
    // Phase II(ii)
    for { // todo correct (differs from paper)?
      (IdeEdge(sp, n), fPrime) <- jumpFunc
      if fPrime != λTop
      if sp.isStartNode
      if !(n.isCallNode || n.isStartNode)
    } {
      vals += n -> vals(n) ⊓ fPrime(vals(sp))
    }
    vals.toMap
  }

  private[this] def computeCallNode(c: IdeNode) {
    val cn = c.n
    for {
      sq                          <- targetStartNodes(cn)
      FactFunPair(dPrime, edgeFn) <- callStartEdges(c, sq)
    } {
      propagateValue(IdeNode(sq, dPrime), edgeFn(vals(c)))
    }
  }

  /**
   * [8-10]
   */
  private[this] def computeStartNode(node: IdeNode, jumpFunc: Map[IdeEdge, IdeFunction]) {
    for {
      c      <- callNodesInProc(enclProc(node.n))
      d2     <- otherSuccEdges(node, c) map { _.d2 }
      target  = IdeNode(c, d2)
      f2      = jumpFunc(IdeEdge(node, target))
      if f2 != λTop
    } {
      propagateValue(target, f2(vals(node)))
    }
  }

  private[this] def propagateValue(n: IdeNode, v: LatticeElem) {
    val ln = vals(n)
    val v2 = v ⊓ ln
    if (v2 != ln) {
      vals += n -> v2
      nodeWorklist enqueue n
    }
  }
}
