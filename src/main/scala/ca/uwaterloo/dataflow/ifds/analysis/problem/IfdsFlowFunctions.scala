package ca.uwaterloo.dataflow.ifds.analysis.problem

import ca.uwaterloo.dataflow.common.ExplodedGraphTypes

trait IfdsFlowFunctions extends ExplodedGraphTypes {

  type IfdsEdgeFn = (XNode, NodeType) => Set[Fact]

  type IfdsOtherEdgeFn = XNode => Set[Fact]

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  def ifdsCallStartEdges: IfdsEdgeFn

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  def ifdsCallReturnEdges: IfdsEdgeFn

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  def ifdsEndReturnEdges: IfdsEdgeFn

  /**
   * Functions for all other (inter-procedural) edges.
   */
  def ifdsOtherSuccEdges: IfdsOtherEdgeFn

  /**
   * Functions for edges to phi instructions
   */
  def ifdsOtherSuccEdgesPhi: IfdsOtherEdgeFn
}
