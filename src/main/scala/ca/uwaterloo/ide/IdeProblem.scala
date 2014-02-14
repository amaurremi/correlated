package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS._

/**
 * @tparam V type of IdeFunction implementation
 */
trait IdeProblem[T, P, F, V] extends TabulationProblem[T, P, F]{

  /**
   * Represents λl.⊤
   */
  val Top: V

  /**
   * Represents λl.l
   */
  val Id: V

  val superGraph: ISupergraph[T, P]

  val superGraphInfo = new SupergraphInfo[T, P, V](getSupergraph)
}
