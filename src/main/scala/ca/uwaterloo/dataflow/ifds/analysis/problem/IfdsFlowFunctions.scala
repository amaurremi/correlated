package ca.uwaterloo.dataflow.ifds.analysis.problem

import ca.uwaterloo.dataflow.common.ExplodedGraphTypes

trait IfdsFlowFunctions extends ExplodedGraphTypes {

  type IfdsEdgeFn = (XNode, NodeType) => Set[Fact]

  type IfdsOtherEdgeFn = XNode => Set[Fact]

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  def ifdsCallStartEdges(node: XNode, tpe: NodeType): Set[Fact]

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  def ifdsCallReturnEdges(node: XNode, tpe: NodeType): Set[Fact]

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  def ifdsEndReturnEdges(node: XNode, tpe: NodeType): Set[Fact]

  /**
   * Functions for all other (inter-procedural) edges.
   */
  def ifdsOtherSuccEdges(node: XNode): Set[Fact]

  /**
   * Functions for edges to phi instructions
   */
  def ifdsOtherSuccEdgesPhi(node: XNode): Set[Fact]
}
