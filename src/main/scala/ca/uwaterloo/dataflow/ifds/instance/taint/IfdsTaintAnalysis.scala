package ca.uwaterloo.dataflow.ifds.instance.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.analysis.typeInference.TypeInference
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

  override def ifdsOtherSuccEdges: IfdsEdgeFn =
    (ideN1, n2) => {
      val n1            = ideN1.n
      val d1            = ideN1.d
      val defaultResult = Set(d1)
      val method        = n1.getMethod
      n1.getLastInstruction match {
        case returnInstr: SSAReturnInstruction if hasRetValue(returnInstr)         =>
          d1 match {
            // we are returning a secret value, because an existing (i.e. secret) fact d1 is returned
            case v@Variable(m, _)
              if isFactReturned(v, n1, returnInstr.getResult) =>
                (methodToReturnVars get m).asScala.toSet + d1
            case _                                            =>
              defaultResult
          }
        // Arrays
        case storeInstr: SSAArrayStoreInstruction
          if factSameAsVar(d1, method, storeInstr.getValue)                        =>
            defaultResult + ArrayElement
        case loadInstr: SSAArrayLoadInstruction if d1 == ArrayElement              =>
          val inference = getTypeInference(enclProc(n1))
          if (isSecretArrayElementType(inference.getType(loadInstr.getDef).getTypeReference))
            defaultResult + Variable(method, loadInstr.getDef)
          else
            defaultResult
        //  Fields
        case putInstr: SSAPutInstruction if factSameAsVar(d1, method, putInstr.getVal) =>
          defaultResult + Field(getIField(method.getClassHierarchy, putInstr.getDeclaredField))
        case getInstr: SSAGetInstruction                                               =>
          d1 match {
            case Field(field)
              if field == getIField(method.getClassHierarchy, getInstr.getDeclaredField) =>
                defaultResult + Variable(method, getInstr.getDef)
            case _                                                                       =>
              defaultResult
          }
        // Casts
        case castInstr: SSACheckCastInstruction
          if factSameAsVar(d1, method, castInstr.getVal)                               =>
            defaultResult + Variable(method, castInstr.getDef)
        case _                                                                         =>
          defaultResult
      }
    }

  lazy val getTypeInference: Procedure => TypeInference =
    proc =>
      TypeInference.make(proc.getIR, true)

  /**
   * For a fact, checks whether the right-hand side of the assignment instruction in node 'n' is the value of the fact.
   * For example, for an assignment
   *   int x = a
   * this checks whether 'a' has the same value number as 'fact' (and corresponds to the same method).
   */
  private[this] def factSameAsVar(fact: Fact, method: IMethod, vn: ValueNumber) =
    fact == Variable(method, vn)

  override def ifdsEndReturnEdges: IfdsEdgeFn =
    (ideN1, _) =>
      ideN1.d match {
        case Variable(method, _) if method == ideN1.n.getMethod => Set.empty
        case _                                                  => Set(ideN1.d)
      }

  override def ifdsCallReturnEdges: IfdsEdgeFn =  // todo should we remove fields as it's done in the IFDS paper?
    (ideN1, _) => {
      val default = Set(ideN1.d)
      val n1 = ideN1.n
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction if !callInstr.isStatic =>
          val receiver = callInstr.getReceiver
          val method = n1.getMethod
          if (factSameAsVar(ideN1.d, method, receiver)) {
            val callVN = callValNum(callInstr)
            getOperationType(callInstr.getDeclaredTarget) match {
              case Some(opType) =>
                opType match {
                  case ReturnsSecretValue    =>
                    default + Variable(method, callVN.get)
                  case ReturnsSecretArray     =>
                    default + ArrayElement
                }
              case None          =>
                default
            }
          }
          else default
        case _                                                       =>
          default
      }
    }

  override def ifdsCallStartEdges: IfdsEdgeFn =
    (ideN1, n2) => {
      val n1            = ideN1.n
      val d1            = ideN1.d
      val targetMethod  = n2.getMethod
      val callerMethod  = n1.getMethod
      val defaultResult = Set(d1)
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction if isSecret(targetMethod)                    =>
          if (d1 == Λ) {
            val valNum: ValueNumber = callValNum(callInstr).get
            val phis = getPhis(n1, valNum, callerMethod)
            defaultResult + Variable(callerMethod, valNum) ++ phis
          } else defaultResult
        case callInstr: SSAInvokeInstruction
          if callInstr.getDeclaredTarget.getDeclaringClass.getName.toString == secretType => // todo is this enough to check that we're invoking a library call?
            Set.empty
        case callInstr: SSAInvokeInstruction                                              =>
          getParameterNumber(ideN1, callInstr) match { // checks if we are passing d1 as an argument to the function
            case Some(argNum)                                       =>
              val substituteFact = Variable(targetMethod, getValNumFromParameterNum(n2, argNum))
              Set(substituteFact)
            case None if d1 == Λ && callValNum(callInstr).isDefined =>
              methodToReturnVars.put(targetMethod, Variable(callerMethod, callValNum(callInstr).get))
              defaultResult
            case None                                               =>
              defaultResult
          }
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
