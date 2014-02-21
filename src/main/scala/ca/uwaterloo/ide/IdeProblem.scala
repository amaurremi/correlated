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
  val edgeFnMap: EdgeFn[T, V]

  val explodedGraphInfo = new ExplodedGraphInfo[T, P, V](getSupergraph, edgeFnMap)

  // input should have zero element, main methods can be taken from WALA
}
