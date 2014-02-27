package ca.uwaterloo.ide

import scala.collection.mutable

// p. 149 of Sagiv, Reps, Horwitz, "Precise interprocedural dataflow analysis
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
        computeStartNode(enclProc(node.n), jumpFunc)
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
    } {
      vals += t -> (vals(t) ⊓ f(vals(e.source)))
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

  private[this] def computeStartNode(p: Procedure, jumpFunc: Map[IdeEdge, IdeFunction]) {
    for {
      c <- getCallIdeNodes(p)
      e <- edgesWithTarget(c)
      f2 = jumpFunc(e)
      if f2 != Top
    } {
      propagateValue(e.target, f2(vals(e.source)))
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
