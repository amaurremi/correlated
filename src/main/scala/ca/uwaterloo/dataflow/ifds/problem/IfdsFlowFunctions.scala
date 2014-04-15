package ca.uwaterloo.dataflow.ifds

import ca.uwaterloo.dataflow.common.ExplodedGraphTypes

trait IfdsFlowFunctions extends ExplodedGraphTypes {

  type IfdsEdgeFn = (XNode, Node) => Set[Fact]

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
  def ifdsOtherSuccEdges: IfdsEdgeFn
}
