package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.{TabulationResult, TabulationSolver}

class IdeSolver[T, P, F, V <: IdeFunction[V]](
  problem: IdeProblem[T, P, F, V]
) extends TabulationSolver[T, P, F](
  problem,
  null
) {

  override def solve(): TabulationResult[T, P, F] = {
    new ComputeJumpFuncs[T, P, F, V](makeWorklist(), problem).run()
    new ComputeValues[T, P, F, V](problem).run()
    ???
  }
}
