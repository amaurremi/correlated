package ca.uwaterloo.id.ifds.analysis.taint

import ca.uwaterloo.id.common.VariableFacts
import ca.uwaterloo.id.ifds.IfdsProblem
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ssa.{SSAInvokeInstruction, SSAReturnInstruction}
import com.ibm.wala.types.MethodReference
import com.ibm.wala.util.collections.HashSetMultiMap
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._

class IfdsTaintAnalysis(fileName: String) extends IfdsProblem with VariableFacts {

  private[this] val config =
    ConfigFactory.load(
      "ide/analysis/taint/" + fileName,
      ConfigParseOptions.defaults.setAllowMissing(false),
      ConfigResolveOptions.defaults
    )

  private[this] val builder              = FlexibleCallGraphBuilder()(config)
  private[this] val callGraph: CallGraph = builder.cg

  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[Node] = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure

  override def getValNum(factElem: ValueNumber, node: XNode): ValueNumber = factElem

  override type FactElem = ValueNumber
  override val O: Fact   = Λ

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def ifdsOtherSuccEdges: IfdsEdgeFn =
    (ideN1, n2) => {
      val n1 = ideN1.n
      val d1 = ideN1.d
      n1.getLastInstruction match {
        case returnInstr: SSAReturnInstruction if hasRetValue(returnInstr) =>
          d1 match {
            // we are returning a secret value, because an existing (i.e. secret) fact d1 is returned
            case v@Variable(m, _) if isFactReturned(v, n1, returnInstr.getResult) =>
              methodToReturnVars.get(m).asScala.toSet + d1
            case _                                                                =>
              Set(d1)
          }
        case _                                                             =>
          Set(d1)
      }
    }

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def ifdsEndReturnEdges: IfdsEdgeFn =
    (ideN1, _) =>
      Set(ideN1.d)

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def ifdsCallReturnEdges: IfdsEdgeFn =
    (ideN1, _) =>
      Set(ideN1.d) // todo not for fields/static variables

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def ifdsCallStartEdges: IfdsEdgeFn =
    (ideN1, n2) => {
      val n1 = ideN1.n
      val d1 = ideN1.d
      val targetMethod = n2.getMethod
      val callerMethod = n1.getMethod
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction if isSecret(targetMethod.getReference) =>
          if (d1 == Λ)
            Set(d1) + Variable(callerMethod, callValNum(callInstr).get)
          else
            Set(d1)
        case callInstr: SSAInvokeInstruction                           =>
          getParameterNumber(ideN1, callInstr) match { // checks if we are passing d1 as an argument to the function
            case Some(argNum)                                       =>
              val substituteFact = Variable(targetMethod, getValNumFromParameterNum(n2, argNum))
              Set(substituteFact)
            case None if d1 == Λ && callValNum(callInstr).isDefined =>
              methodToReturnVars.put(targetMethod, Variable(callerMethod, callValNum(callInstr).get)) // todo is this the right way to keep track of return variables?
              Set(d1)
            case None                                               =>
              Set(d1)
          }
        case _                                                         =>
          throw new UnsupportedOperationException("callStartEdges invoked on non-call instruction")
      }
    }

  private[this] val methodToReturnVars = new HashSetMultiMap[IMethod, Variable]

  def isSecret(method: MethodReference) = method.getName.toString == "secret"

  private[this] def isFactReturned(d: Variable, n: Node, retVal: ValueNumber): Boolean =
    d.elem == retVal && d.method == n.getMethod
}
