package ca.uwaterloo.dataflow.ifds.instance.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ssa._
import com.ibm.wala.util.collections.HashSetMultiMap
import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import scala.collection.JavaConverters._

abstract class IfdsTaintAnalysis(fileName: String) extends IfdsProblem with VariableFacts with SecretDefinition {

  private[this] val config =
    ConfigFactory.load(
      "ca/uwaterloo/dataflow/ide/taint/" + fileName,
      ConfigParseOptions.defaults.setAllowMissing(false),
      ConfigResolveOptions.defaults
    )

  private[this] val builder = FlexibleCallGraphBuilder()(config)

  override val callGraph: CallGraph = builder.cg
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
      val n1            = ideN1.n
      val d1            = ideN1.d
      val defaultResult = Set(d1)
      val method        = n1.getMethod
      n1.getLastInstruction match {
        case returnInstr: SSAReturnInstruction if hasRetValue(returnInstr) =>
          d1 match {
            // we are returning a secret value, because an existing (i.e. secret) fact d1 is returned
            case v@Variable(m, _) if isFactReturned(v, n1, returnInstr.getResult) =>
              (methodToReturnVars get m).asScala.toSet + d1
            case _                                                                =>
              defaultResult
          }
        // Arrays
        case storeInstr: SSAArrayStoreInstruction if factIsRval(d1, method, storeInstr.getValue) =>
          defaultResult + ArrayElement
        case loadInstr: SSAArrayLoadInstruction
          if d1 == ArrayElement && isSecretSupertype(loadInstr.getElementType)      =>
            defaultResult + Variable(method, loadInstr.getDef)
        //  Fields
        case putInstr: SSAPutInstruction if factIsRval(d1, method, putInstr.getVal) =>
          defaultResult + Field(putInstr.getDeclaredField)
        case getInstr: SSAGetInstruction                                   =>
          d1 match {
            case Field(field) if field == getInstr.getDeclaredField =>
              defaultResult + Variable(method, getInstr.getDef)
            case _                                                  =>
              defaultResult
          }
        // Casts
        case castInstr: SSACheckCastInstruction if factIsRval(d1, method, castInstr.getVal) =>
          defaultResult + Variable(method, castInstr.getDef)
        case _                                                             =>
          defaultResult
      }
    }

  /**
   * For a fact, checks whether the right-hand side of the assignment instruction in node 'n' is the value of the fact.
   * For example, for an assignment
   *   int x = a
   * this checks whether 'a' has the same value number as 'fact' (and corresponds to the same method).
   */
  private[this] def factIsRval(fact: Fact, method: IMethod, vn: ValueNumber) =
    fact == Variable(method, vn)

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def ifdsEndReturnEdges: IfdsEdgeFn =
    (ideN1, _) =>
      ideN1.d match {
        case Variable(method, _) if method == ideN1.n.getMethod => Set.empty
        case _                                                  => Set(ideN1.d)
      }

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
      val n1            = ideN1.n
      val d1            = ideN1.d
      val targetMethod  = n2.getMethod
      val callerMethod  = n1.getMethod
      val defaultResult = Set(d1)
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction if isSecret(targetMethod.getReference) =>
          if (d1 == Λ) {
            val valNum: ValueNumber = callValNum(callInstr).get
            val phis = getPhis(n1, valNum, callerMethod)
            defaultResult + Variable(callerMethod, valNum) ++ phis
          } else defaultResult
        case callInstr: SSAInvokeInstruction                           =>
          getParameterNumber(ideN1, callInstr) match { // checks if we are passing d1 as an argument to the function
            case Some(argNum)                                       =>
              val substituteFact = Variable(targetMethod, getValNumFromParameterNum(n2, argNum))
              Set(substituteFact)
            case None if d1 == Λ && callValNum(callInstr).isDefined =>
              methodToReturnVars.put(targetMethod, Variable(callerMethod, callValNum(callInstr).get)) // todo is this the right way to keep track of return variables?
              defaultResult
            case None                                               =>
              defaultResult
          }
        case _                                                         =>
          throw new UnsupportedOperationException("callStartEdges invoked on non-call instruction")
      }
    }

  private[this] def getPhis(node: Node, valNum: ValueNumber, method: IMethod): Set[Fact] =
    (enclProc(node).getIR.iteratePhis().asScala collect {
      case phiInstr: SSAPhiInstruction if phiInstr.getUse(0) == valNum || phiInstr.getUse(1) == valNum =>
        Variable(method, phiInstr.getDef)
    }).toSet

  private[this] val methodToReturnVars = new HashSetMultiMap[IMethod, Variable]

  private[this] def isFactReturned(d: Variable, n: Node, retVal: ValueNumber): Boolean =
    d.elem == retVal && d.method == n.getMethod
}
