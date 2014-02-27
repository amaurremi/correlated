package ca.uwaterloo.ide

import scala.collection.JavaConverters._

trait ExplodedGraphInfo { this: ExplodedGraphTypes =>

  def followingNodes(n: Node): Iterator[Node] =
    supergraph.getSuccNodes(n).asScala

  lazy val ideNodes: Node => Set[IdeNode] = ???

  /**
   * All edges with a given target node.
   */
  lazy val edgesWithTarget: IdeNode => Set[IdeEdge] = ???

  /**
   * Returns the enclosing procedure of a given node.
   */
  def enclProc: Node => Procedure = supergraph.getProcOf

  /**
   * Given a call node n, returns the start nodes of n's target procedures.
   */
  def targetStartNodes(n: Node): Iterator[Node] =
    supergraph.getCalledNodes(n).asScala

  def returnNodes(n: Node): Iterator[Node] = {
    targetStartNodes(n) flatMap { s =>
      supergraph.getReturnSites(n, enclProc(s)).asScala
    }
  }

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: Node => Iterator[Node] = { // todo: in general, not sure to which scala collections WALA's collections should be converted
    n =>
      val nodes = supergraph getEntriesForProcedure enclProc(n)
      nodes.view.toIterator
  }

  def callReturnPairs(node: Node): Seq[(Node, Node)] = {
    val proc = enclProc(node)
    for {
      c <- getCallNodes(proc)
      r <- supergraph.getReturnSites(c, proc).asScala
    } yield c -> r
  }

  /**
   * All corresponding call-return edges in the exploded graph.
   */
  private[this] lazy val allCallReturnPairs: Iterator[(Node, Node)] = {
    val callToManyProcs = supergraphIterator collect {
      case node if supergraph.isCall(node) =>
        node -> (supergraph.getCalledNodes(node).asScala map enclProc)
    }
    for {
      (c, ps) <- callToManyProcs
      p       <- ps
      r       <- supergraph.getReturnSites(c, p).asScala
    } yield c -> r
  }

  lazy val allCallReturnIdeEdges: Set[IdeEdge] = ??? // todo: findOrCreate

  /**
   * All intra-procedural edges from the start of a procedure.
   */
  def intraEdgesFromStart: Set[IdeEdge] = ??? // todo: findOrCreate

  private[this] def intraNodePairsFromStart: Iterator[(Node, Node)] = {
    val procs = supergraph.getProcedureGraph.iterator.asScala
    for {
      p <- procs
      s <- supergraph.getEntriesForProcedure(p)
      n <- nodesInProc(s, p, Seq.empty)
    } yield s -> n
  }

  /**
   * All intra-procedural nodes from the start of a procedure.
   */
  private[this] def nodesInProc(startNode: Node, proc: Procedure, acc: Seq[Node]): Seq[Node] = // todo not sure this is correct either
    if (enclProc(startNode) != proc)
      acc
    else supergraph.getSuccNodes(startNode).asScala.toSeq flatMap {
      nodesInProc(_, proc, acc :+ startNode)
    }

  /**
   * Returns all call nodes for a given procedure.
   */
  lazy val getCallNodes: Procedure => Seq[Node] = {
    proc =>
      val procEntryNodes = supergraph getEntriesForProcedure proc flatMap ideNodes
      procEntryNodes flatMap edgesWithTarget collect { // todo shouldn't be done through IdeEdge
        case IdeEdge(s, _) if s.isCallNode => s.n
      }
  }

  /**
   * Returns all coll IDE nodes for a given procedure.
   */
  lazy val getCallIdeNodes: Procedure => Seq[IdeNode] =
    getCallNodes(_) flatMap ideNodes

  /**
   * Returns an iterator over the exploded graph that corresponds to the supergraph.
   */
  lazy val explodedGraphIterator: Iterator[IdeNode] =
    supergraphIterator flatMap ideNodes

  private[this] def supergraphIterator: Iterator[Node] =
    supergraph.iterator.asScala

  /**
   * All nodes that are not call nodes or start nodes.
   */
  lazy val notCallOrStartNodes: Iterator[IdeNode] =
    explodedGraphIterator filterNot {
      n =>
        n.isCallNode || n.isStartNode
    }
}
