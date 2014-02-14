package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS._

case class IdeProblem[T, P, F](
  superGraph: ISupergraph[T, P],
  domain: TabulationDomain[F, T],
  initialSeeds: Seq[PathEdge[T]]
) extends TabulationProblem[T, P, F]{

  override def getMergeFunction: IMergeFunction = null

  override def getFunctionMap: IFlowFunctionMap[T] = ???

  override def getDomain: TabulationDomain[F, T] = domain

  override def getSupergraph: ISupergraph[T, P] = superGraph

  val superGraphInfo = new SupergraphInfo[T, P](superGraph)
}
