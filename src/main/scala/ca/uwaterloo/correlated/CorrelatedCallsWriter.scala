package ca.uwaterloo.correlated

import ca.uwaterloo.correlated.util.Converter._
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.classLoader.CallSiteReference
import scalaz.{Scalaz, Applicative, Semigroup, Writer}

object CorrelatedCallsWriter {

  /**
   * Traverses the call graph writing relevant information to CorrelatedCalls
   */
  def apply(
             rcs: List[Set[CGNode]], cgNodes: List[CGNode]
             ): CorrelatedCallWriter[_] = {
    import Scalaz._

    for {
      _ <- CorrelatedCalls(rcs = rcs).tell
      _ <- cgNodes.traverse[CorrelatedCallWriter, CGNode](cgNodeWriter(rcs.flatten.toSet))
    } yield ()
  }

  /**
   * The implicit functions 's' and 'applicative' are necessary to use scalaz's Writer monad.
   */
  implicit val s = new Semigroup[CorrelatedCalls]{
    def append(f1: CorrelatedCalls, f2: => CorrelatedCalls): CorrelatedCalls =
      CorrelatedCalls(
        f1.cgNodes ++ f2.cgNodes,
        f1.rcs ++ f2.rcs,
        f1.rcCcReceivers ++ f2.rcCcReceivers,
        f1.receiverToCallSites ++ f2.receiverToCallSites,
        f1.totalCallSites ++ f2.totalCallSites
      )
  }

  implicit val applicative = new Applicative[CorrelatedCallWriter] {
    def point[A](a: => A): CorrelatedCallWriter[A] = Writer(CorrelatedCalls.empty, a)

    def ap[A, B](fa: => CorrelatedCallWriter[A])(f: => CorrelatedCallWriter[(A) => B]): CorrelatedCallWriter[B] =
      for {
        a  <- fa
        f2 <- f
      } yield f2(a)
  }

  /**
   * Traverses the call sites of a call graph node writing relevant information to CorrelatedCalls
   */
  private[this] def cgNodeWriter(
    rcs: Set[CGNode]
  )(
    cgNode: CGNode
  ): CorrelatedCallWriter[CGNode] = {
    import Scalaz._

    val recToCallSites = receiverToCallSites(cgNode)
    for {
      _ <- CorrelatedCalls(
        cgNodes             = Set(cgNode),
        totalCallSites      = callSiteIterator(cgNode).toSet,
        receiverToCallSites = recToCallSites,
        rcCcReceivers       = if (rcs contains cgNode) recToCallSites.keys.toSet else Set.empty
      ).tell
    } yield cgNode
  }

  private[this] def receiverToCallSites(
    cgNode: CGNode
  ): MultiMap[Receiver, CallSiteReference] = {
    val startMap = Map[Receiver, Set[CallSiteReference]]() withDefaultValue Set.empty
    // Create a map from receivers to dispatch call sites
    val receiverToCallsiteMap = callSiteIterator(cgNode).foldLeft(startMap) {
      (map, callSite) =>
        Receiver(cgNode, callSite) match {
          case Some(receiver) =>
            map.updated(receiver, map(receiver) + callSite)
          case _ =>
            map
        }
    }
    // Return a map that only contains receivers with more than one corresponding call site
    receiverToCallsiteMap filter {
      case (_, set) => set.size > 1
    }
  }

  private[this] def callSiteIterator(cgNode: CGNode): Iterator[CallSiteReference] = {
    toScalaIterator(cgNode.iterateCallSites())
  }
}
