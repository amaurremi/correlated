package ca.uwaterloo.id.ifds

import ca.uwaterloo.id.common.ExplodedGraphTypes

trait IfdsFlowFunctions extends ExplodedGraphTypes {

  type IfdsEdgeFn = (XNode, Node) => Set[Fact]

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  def callStartEdges: IfdsEdgeFn

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  def callReturnEdges: IfdsEdgeFn

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  def endReturnEdges: IfdsEdgeFn

  /**
   * Functions for all other (inter-procedural) edges.
   */
  def otherSuccEdges: IfdsEdgeFn
}
