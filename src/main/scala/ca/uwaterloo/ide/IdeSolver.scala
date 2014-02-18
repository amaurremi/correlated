package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.{PathEdge, TabulationProblem, TabulationResult, TabulationSolver}
import com.ibm.wala.util.intset.IntSet
import java.util

class IdeSolver[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V]
) extends TabulationSolver[T, P, F](
  problem,
  null
) {

  // todo: modify edges in super graph?
  override def solve(): TabulationResult[T, P, F] = {
    val jumpFuncs = new JumpFuncs[T, P, F, V](makeWorklist(), problem).compute
    val vals = new ComputeValues[T, P, F, V](problem, jumpFuncs).compute
    valuesToResult(vals)
  }

  private[this] def valuesToResult(vals: Values[T]): TabulationResult[T, P, F] =
    new TabulationResult[T, P, F] {
      override def getSeeds: util.Collection[PathEdge[T]] = ???

      override def getSummaryTargets(n1: T, d1: Int, n2: T): IntSet = ???

      override def getSupergraphNodesReached: util.Collection[T] = ???

      override def getProblem: TabulationProblem[T, P, F] = ???

      override def getResult(node: T): IntSet = ???
    }
}
