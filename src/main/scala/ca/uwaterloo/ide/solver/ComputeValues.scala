package ca.uwaterloo.ide

import scala.collection.mutable

// p. 149 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
// with applications to constant propagation"
trait ComputeValues { this: IdeProblem with TraverseGraph =>

  private[this] val vals = mutable.Map[IdeNode, LatticeNum]()

  private[this] lazy val nodeWorklist = mutable.Queue[IdeNode]()

  private[this] def initialize() {
    nodeWorklist += ??? // todo findOrCreate
    // [1-2]
    vals += ??? // todo findOrCreate
  }

  def computeValues(jumpFunc: Map[IdeEdge, IdeFunction]): Map[IdeNode, LatticeNum]  = {
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
      // todo MARIANNA works only if JumpFn doesn't contain tops
      e <- jumpFunc.keys
      sp = e.source
      if sp.isStartNode
      n = e.target
      if !(n.isCallNode || n.isStartNode)
      fPrime = jumpFunc(e)
      if fPrime != Top // todo should be unnecessary
    } {
      vals += n -> vals(n) ⊓ fPrime(vals(sp))
    }
    vals.toMap
  }

  private[this] def computeCallNode(c: IdeNode) {
    val cn = c.n
    val cd = c.d
    for {
      sq                          <- targetStartNodes(cn)
      FactFunPair(dPrime, edgeFn) <- callStartFns(cn, cd, sq)
    } {
      propagateValue(IdeNode(sq, dPrime), edgeFn(vals(IdeNode(cn, cd))))
    }
  }

  /**
   * [8-10]
   */
  private[this] def computeStartNode(node: IdeNode, jumpFunc: Map[IdeEdge, IdeFunction]) {
    for {
      c      <- callNodesInProc(enclProc(node.n))
      d2     <- otherSuccEdges(node.n, node.d, c) map { _.d2 }
      target  = IdeNode(c, d2)
      f2      = jumpFunc(IdeEdge(node, target))
      if f2 != Top
    } {
      propagateValue(target, f2(vals(node)))
    }
  }

  private[this] def propagateValue(n: IdeNode, v: LatticeNum) {
    val ln = vals(n)
    val v2 = v ⊓ ln
    if (v2 != ln) {
      vals += n -> v2
      nodeWorklist enqueue n
    }
  }
}
