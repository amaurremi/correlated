package ca.uwaterloo.id.ifds.analysis.taint

import ca.uwaterloo.id.ifds.IfdsProblem
import com.ibm.wala.dataflow.IFDS.ISupergraph

class IfdsTaintAnalysis extends IfdsProblem {
  override val O: Fact = ZeroFact
  override type Fact = TaintFact
  override val entryPoints: Seq[Node] = ???
  override val supergraph: ISupergraph[Node, Procedure] = ???
  override type Procedure = this.type
  override type Node = this.type

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: IfdsEdgeFn = ???

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: IfdsEdgeFn = ???

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: IfdsEdgeFn = ???

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartEdges: IfdsEdgeFn = ???

  trait TaintFact

  private[this] object ZeroFact extends TaintFact
}
