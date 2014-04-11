package ca.uwaterloo.id.ide

import scala.collection.{breakOut, mutable}

// p. 149 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
// with applications to constant propagation"
trait ComputeValues { this: IdeProblem with TraverseGraph =>

  private[this] type JumpFn = Map[IdeEdge, IdeFunction]

  /**
   * [1]
   */
  private[this] val vals = mutable.Map[IdeNode, LatticeElem]() withDefault { _ => Top }

  private[this] lazy val nodeWorklist = mutable.Queue[IdeNode]()

  private[this] def initialize() {
    // [2]
    vals ++= (entryPoints map {
      IdeNode(_, Λ) -> Bottom
    })(breakOut)
    // [3]
    nodeWorklist ++= entryPoints map { IdeNode(_, Λ) }
  }

  def computeValues(jumpFunc: JumpFn): Map[IdeNode, LatticeElem]  = {
    // Phase II(i)
    initialize()
    // [4-17]
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

  private[this] def getJumpFnTargetFacts(ideNode1: IdeNode, node2: Node, jumpFn: JumpFn): Set[FactFunPair] =
    (jumpFn collect {
      case (IdeEdge(source, IdeNode(n, d)), f)
        if source == ideNode1 && n == node2 =>
          FactFunPair(d, f)
    })(breakOut)

  /**
   * [8-10]
   */
  private[this] def computeStartNode(sp: IdeNode, jumpFunc: JumpFn) {
    for {
      c                           <- callNodesInProc(enclProc(sp.n))
      FactFunPair(dPrime, fPrime) <- getJumpFnTargetFacts(sp, c, jumpFunc)
      if fPrime != λTop
    } {
      propagateValue(IdeNode(c, dPrime), fPrime(vals(sp)))
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
