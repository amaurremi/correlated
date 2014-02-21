package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.ISupergraph
import scala.collection.JavaConverters._

class ExplodedGraphInfo[T, P, V <: IdeFunction[V]](
  supergraph: ISupergraph[T, P],
  edgeFn: EdgeFn[T, V],
  allFacts: Set[Fact]
) {

  import edgeFn.keys

  /**
   * All edges with a given source.
   */
  lazy val edgesWithSource: IdeNode[T] => Set[IdeEdge[T]] =
    source =>
      keys filter {
        _.source == source
      }

  lazy val ideNodes: T => Set[IdeNode[T]] =
    n =>
      keys collect {
        case edge if edge.source.n == n => edge.source
        case edge if edge.target.n == n => edge.target
      }

  /**
   * All edges with a given target node.
   */
  lazy val edgesWithTarget: IdeNode[T] => Set[IdeEdge[T]] =
    target =>
      keys filter {
        _.target == target
      }

  /**
   * Returns all edges from a given call node to a start node.
   */
  def callStartEdges(n: T): Set[IdeEdge[T]] =
    for {
      node                          <- ideNodes(n)
      e@IdeEdge(_, StartNode(_, _)) <- edgesWithSource(node)
    } yield e

  /**
   * Let p be the node's enclosing procedure. This method returns all
   * edges from p's caller nodes to their corresponding return nodes.
   */
  def callReturnEdges(node: T): Seq[IdeEdge[T]] = { // todo not sure that's the right implementation
    val proc        = enclProc(node)
    val callNodes   = getCallNodes(proc)
    val returnNodes = callNodes map {
      supergraph.getReturnSites(_, proc)
    }
    for {
      n <- callNodes
      c <- ideNodes(n)
      e <- edgesWithSource(c)
      if returnNodes contains e.target
    } yield e
  }

  /**
   * Returns the enclosing procedure of a given node.
   */
  def enclProc: T => P = supergraph.getProcOf

  /**
   * Returns the start node of the argument's enclosing procedure.
   */
  lazy val startNodes: T => Array[IdeNode[T]] = // todo: in general, not sure to which scala collections WALA's collections should be converted
    supergraph getEntriesForProcedure enclProc(_) flatMap ideNodes

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
