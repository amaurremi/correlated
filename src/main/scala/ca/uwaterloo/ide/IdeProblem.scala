package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS._

/**
 * @tparam V type of IdeFunction implementation
 */
trait IdeProblem[T, P, F, V <: IdeFunction[V]] extends TabulationProblem[T, P, F]{

  /**
   * Represents λl.⊤
   */
  val Top: V

  /**
   * Represents λl.l
   */
  val Id: V

  /**
   * The flow function that corresponds to an exploded graph edge
   */
  val edgeFn: EdgeFn[T, V]

  val supergraphInfo = new SupergraphInfo[T, P, V](getSupergraph)
}
