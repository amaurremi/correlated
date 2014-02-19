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
    val jumpFuncs = new JumpFuncs[T, P, F, V](problem).compute
    val vals = new ComputeValues[T, P, F, V](problem, jumpFuncs).compute
    valuesToResult(vals)
  }

  private[this] def valuesToResult(vals: Values[T]): TabulationResult[T, P, F] = ??? // todo: make IdeSolver not extend TabulationSolver
}
