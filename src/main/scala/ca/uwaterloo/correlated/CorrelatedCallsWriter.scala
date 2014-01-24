package ca.uwaterloo.correlated

import scalaz.{Applicative, Semigroup, Writer}

object CorrelatedCallsWriter {

  implicit val s = new Semigroup[CorrelatedCalls]{
    def append(f1: CorrelatedCalls, f2: => CorrelatedCalls): CorrelatedCalls =
      CorrelatedCalls(
        f1.cgNodes + f2.cgNodes,
        f1.sccs ++ f2.sccs,
        f1.sccReceivers ++ f2.sccReceivers,
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
}
