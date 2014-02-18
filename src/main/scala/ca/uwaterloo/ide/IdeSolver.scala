package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.{TabulationResult, TabulationSolver}

class IdeSolver[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V]
) extends TabulationSolver[T, P, F](
  problem,
  null
) {

  // todo: modify edges in super graph?
  override def solve(): TabulationResult[T, P, F] = {
    val jumpFuncs = new JumpFuncs[T, P, F, V](makeWorklist(), problem).compute
    new ComputeValues[T, P, F, V](problem, jumpFuncs).compute
    ???
  }
}
