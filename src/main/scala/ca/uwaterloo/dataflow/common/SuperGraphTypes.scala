package ca.uwaterloo.dataflow.common

import com.ibm.wala.dataflow.IFDS.ISupergraph
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis

trait SuperGraphTypes {

  sealed abstract class NodeType(val node: Node)
  case class NormalNode(override val node: Node) extends NodeType(node)
  case class PhiNode(override val node: Node) extends NodeType(node)

  /**
   * Type of a node in the WALA supergraph
   */
  type Node

  /**
   * Type of a procedure for the WALA supergraph
   */
  type Procedure

  /**
   * The supergraph generated by WALA
   */
  val supergraph: ISupergraph[Node, Procedure]

  /**
   * Pointer analysis generated by WALA
   */
  val pointerAnalysis: PointerAnalysis

  /**
   * The call graph generated by WALA
   */
  val callGraph: CallGraph

  /**
   * The main method nodes that should be the entry points for the instance
   */
  val entryPoints: Seq[NodeType]
}
