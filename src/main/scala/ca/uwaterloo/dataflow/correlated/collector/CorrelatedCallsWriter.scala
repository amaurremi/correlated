package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.MultiMap
import com.ibm.wala.ipa.callgraph.{CGNode, CallGraph}
import com.ibm.wala.util.graph.traverse.DFS

import scalaz.{Applicative, Semigroup, Writer}

object CorrelatedCallsWriter {

  import ca.uwaterloo.dataflow.correlated.collector.util.Converter._

  /**
   * Traverses the call graph writing relevant information to CorrelatedCalls
   */
  def apply(
    cg: CallGraph
  ): CorrelatedCallWriter[_] = {
    import ca.uwaterloo.dataflow.correlated.collector.util.CallGraphUtil.getRcs

import scalaz.Scalaz._

    val cgNodes = toScalaList(DFS.getReachableNodes(cg).iterator)
    val rcs = getRcs(cg)
    for {
      _ <- CorrelatedCallStats(
        rcs     = rcs,
        cgNodes = cgNodes.toSet
      ).tell
      _ <- cgNodes.traverse[CorrelatedCallWriter, CGNode](cgNodeWriter(cg, rcs.flatten.toSet))
    } yield ()
  }

  /**
   * Traverses the call sites of a call graph node writing relevant information to CorrelatedCalls
   */
  private[this] def cgNodeWriter(
    cg: CallGraph,
    rcs: Set[CGNode]
  )(
    cgNode: CGNode
  ): CorrelatedCallWriter[CGNode] = {
    import scalaz.Scalaz._

    val callSites = toScalaIterator(cgNode.iterateCallSites()).toList map { (_, cgNode) }
    for {
      maps  <- callSites.traverse[CorrelatedCallWriter, ReceiverToCallSites](callSiteWriter(cg, rcs))
      ccMap  = getCcMap(maps)
      _     <- CorrelatedCallStats(
        totalCallSites      = callSites.toSet,
        receiverToCallSites = ccMap,
        rcCcReceivers       = if (rcs contains cgNode) ccMap.keySet else Set.empty
      ).tell
    } yield cgNode
  }


  private[this] def getCcMap(maps: List[ReceiverToCallSites]): ReceiverToCallSites = {
    MultiMap.mergeMultiMapList(maps) filter {
      case (_, set) =>
        set.size > 1
    }
  }

  private[this] def callSiteWriter(
    cg: CallGraph,
    rcs: Set[CGNode]
  )(
    callSite: CallSite
  ): CorrelatedCallWriter[ReceiverToCallSites] = {
    import scalaz.Scalaz._

    Receiver(cg, callSite) match {
      case Some(receivers) =>
        val receiverToCallSite = (receivers map { (_, Set(callSite))}).toMap
        for {
          _ <- CorrelatedCallStats(
            polymorphicCallSites = Set(callSite)
          ).tell
        } yield receiverToCallSite
      case None           =>
        for {
          _ <- CorrelatedCallStats(
            monomorphicCallSites = Set(callSite)
          ).tell
        } yield Map.empty[Receiver, Set[CallSite]]
    }
  }

  /**
   * The implicit functions 's' and 'applicative' are necessary to use scalaz's Writer monad.
   */
  implicit val s = new Semigroup[CorrelatedCallStats]{

    override def append(f1: CorrelatedCallStats, f2: => CorrelatedCallStats): CorrelatedCallStats =
      CorrelatedCallStats(
        f1.cgNodes ++ f2.cgNodes,
        f1.rcs ++ f2.rcs,
        f1.rcCcReceivers ++ f2.rcCcReceivers,
        MultiMap.mergeMultiMaps(f1.receiverToCallSites, f2.receiverToCallSites),
        f1.totalCallSites ++ f2.totalCallSites,
        f1.polymorphicCallSites ++ f2.polymorphicCallSites,
        f1.monomorphicCallSites ++ f2.monomorphicCallSites
      )
  }

  implicit val applicative = new Applicative[CorrelatedCallWriter] {
    override def point[A](a: => A): CorrelatedCallWriter[A] = Writer(CorrelatedCallStats.empty, a)

    override def ap[A, B](
      fa: => CorrelatedCallWriter[A]
    )(
      f: => CorrelatedCallWriter[(A) => B]
    ): CorrelatedCallWriter[B] =
      for {
        a  <- fa
        f2 <- f
      } yield f2(a)
  }
}
