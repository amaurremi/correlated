package ca.uwaterloo.ide

import scala.collection.mutable

class ComputeValues[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V]
) {

  import problem._
  import supergraphInfo._

  private[this] val initializeVals: mutable.Map[T, LatticeNum] =
    mutable.Map(supergraphIterator.map {
      (_, ⊤)
    }.toSeq: _*)

  private[this] lazy val vals: mutable.Map[T, LatticeNum] = initializeVals // todo: factor out into separate class

  private[this] lazy val nodeWorklist = new NodeWorklist[T]


  def run() {
    while (!nodeWorklist.isEmpty) {
      nodeWorklist.take().n match {
        case StartNode(_, _, p: P) =>
          getCallnodes(p) map {
            node =>

          }
      }
    }
  }
}
