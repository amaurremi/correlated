package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph
import scala.collection.JavaConverters._

class ExplodedGraphInfo[T, P, V <: IdeFunction[V]](
  supergraph: ISupergraph[T, P],
  allFacts: Set[Fact]
) {

  def followingNodes(n: T): Iterator[T] =
    supergraph.getSuccNodes(n).asScala

  lazy val ideNodes: T => Set[IdeNode[T]] = ???

  /**
   * All edges with a given target node.
   */
  lazy val edgesWithTarget: IdeNode[T] => Set[IdeEdge[T]] = ???

  /**
   * Returns the enclosing procedure of a given node.
   */
  def enclProc: T => P = supergraph.getProcOf

  /**
   * Given a call node n, returns the start nodes of n's target procedures.
   */
  def targetStartNodes(n: T): Iterator[T] =
    supergraph.getCalledNodes(n).asScala

  def returnNodes(n: T): Iterator[T] = {
    targetStartNodes(n) flatMap { s =>
      supergraph.getReturnSites(n, enclProc(s)).asScala
    }
  }

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: T => Array[_ <: T] = // todo: in general, not sure to which scala collections WALA's collections should be converted
    supergraph getEntriesForProcedure enclProc(_)

  def callReturnPairs(node: T): Seq[(T, T)] = {
    val proc = enclProc(node)
    for {
      c <- getCallNodes(proc)
      r <- supergraph.getReturnSites(c, proc).asScala
    } yield c -> r
  }

  /**
   * All corresponding call-return edges in the exploded graph.
   */
  private[this] lazy val allCallReturnPairs: Iterator[(T, T)] = {
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

  lazy val allCallReturnIdeEdges: Set[IdeEdge[T]] = // todo: findOrCreate?
    for {
      d1     <- allFacts
      d2     <- allFacts
      (c, r) <- allCallReturnPairs
    } yield IdeEdge(IdeNode(c, d1, supergraph), IdeNode(r, d2, supergraph))

  /**
   * All intra-procedural edges from the start of a procedure.
   */
  def intraEdgesFromStart: Set[IdeEdge[T]] = { // todo not sure this is correct
    for {
      d1     <- allFacts
      d2     <- allFacts
      (s, n) <- intraNodePairsFromStart
    } yield IdeEdge(IdeNode(s, d1, supergraph), IdeNode(n, d2, supergraph))
  }

  private[this] def intraNodePairsFromStart: Iterator[(T, T)] = {
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
  private[this] def nodesInProc(startNode: T, proc: P, acc: Seq[T]): Seq[T] = // todo not sure this is correct either
    if (enclProc(startNode) != proc)
      acc
    else supergraph.getSuccNodes(startNode).asScala.toSeq flatMap {
      nodesInProc(_, proc, acc :+ startNode)
    }

  /**
   * Returns all call nodes for a given procedure.
   */
  lazy val getCallNodes: P => Seq[T] = {
    proc =>
      val procEntryNodes = supergraph getEntriesForProcedure proc flatMap ideNodes
      procEntryNodes flatMap edgesWithTarget collect {
        case IdeEdge(CallNode(c, _), _) => c
      }
  }

  /**
   * Returns all coll IDE nodes for a given procedure.
   */
  lazy val getCallIdeNodes: P => Seq[IdeNode[T]] =
    getCallNodes(_) flatMap ideNodes

  /**
   * Returns an iterator over the exploded graph that corresponds to the supergraph.
   */
  lazy val explodedGraphIterator: Iterator[IdeNode[T]] =
    supergraphIterator flatMap ideNodes


  private[this] def supergraphIterator: Iterator[T] = 
    supergraph.iterator.asScala

  /**
   * All nodes that are not call nodes or start nodes.
   */
  lazy val notCallOrStartNodes: Iterator[IdeNode[T]] =
    explodedGraphIterator filter {
      case CallNode(_, _) | StartNode(_, _) => false
      case _                                => true
    }
}
