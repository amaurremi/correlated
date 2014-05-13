package ca.uwaterloo.dataflow.correlated.collector

import ca.uwaterloo.dataflow.correlated.collector.util.{MultiMap, CallGraphUtil, Converter}
import com.ibm.wala.classLoader.CallSiteReference
import com.ibm.wala.ipa.callgraph.{CallGraph, CGNode}
import com.ibm.wala.util.graph.traverse.DFS
import scala.Some
import scalaz.{Scalaz, Applicative, Semigroup, Writer}

object CorrelatedCallsWriter {

  import Converter._

  /**
   * Traverses the call graph writing relevant information to CorrelatedCalls
   */
  def apply(
    cg: CallGraph
  ): CorrelatedCallWriter[_] = {
    import CallGraphUtil.getRcs
    import Scalaz._

    val cgNodes = toScalaList(DFS.getReachableNodes(cg).iterator)
    val rcs = getRcs(cg)
    for {
      _ <- CorrelatedCalls(
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
    import Scalaz._

    val callSites = toScalaIterator(cgNode.iterateCallSites()).toList
    for {
      maps  <- callSites.traverse[CorrelatedCallWriter, ReceiverToCallSites](callSiteWriter(cg, cgNode, rcs))
      ccMap  = getCcMap(maps)
      _     <- CorrelatedCalls(
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
    cgNode: CGNode,
    rcs: Set[CGNode]
  )(
    callSite: CallSiteReference
  ): CorrelatedCallWriter[ReceiverToCallSites] = {
    import Scalaz._

    Receiver(cg, cgNode, callSite) match {
      case Some(receiver) =>
        val receiverToCallSite = Map(receiver -> Set(callSite))
        for {
          _ <- CorrelatedCalls(
            polymorphicCallSites = Set(callSite)
          ).tell
        } yield receiverToCallSite
      case None           =>
        applicative point Map.empty
    }
  }

  /**
   * The implicit functions 's' and 'applicative' are necessary to use scalaz's Writer monad.
   */
  implicit val s = new Semigroup[CorrelatedCalls]{

    override def append(f1: CorrelatedCalls, f2: => CorrelatedCalls): CorrelatedCalls =
      CorrelatedCalls(
        f1.cgNodes ++ f2.cgNodes,
        f1.rcs ++ f2.rcs,
        f1.rcCcReceivers ++ f2.rcCcReceivers,
        MultiMap.mergeMultiMaps(f1.receiverToCallSites, f2.receiverToCallSites),
        f1.totalCallSites ++ f2.totalCallSites,
        f1.polymorphicCallSites ++ f2.polymorphicCallSites
      )
  }

  implicit val applicative = new Applicative[CorrelatedCallWriter] {
    override def point[A](a: => A): CorrelatedCallWriter[A] = Writer(CorrelatedCalls.empty, a)

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
