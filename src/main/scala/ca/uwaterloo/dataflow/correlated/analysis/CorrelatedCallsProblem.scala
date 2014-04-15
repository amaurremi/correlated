package ca.uwaterloo.dataflow.correlated.analysis

import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem

trait CorrelatedCallsProblem extends CorrelatedCallsProblemBuilder { this: IfdsProblem =>

  override def otherSuccEdges: IdeEdgeFn  = ???
  override def endReturnEdges: IdeEdgeFn  = ???
  override def callReturnEdges: IdeEdgeFn = ???
  override def callStartEdges: IdeEdgeFn  = ???
}
