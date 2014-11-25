package ca.uwaterloo.dataflow.ifds.instance.taint

import ca.uwaterloo.dataflow.common.VariableFacts
import ca.uwaterloo.dataflow.ifds.analysis.problem.IfdsProblem
import com.ibm.wala.analysis.typeInference.{PointType, TypeInference}
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.dataflow.IFDS.{ICFGSupergraph, ISupergraph}
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.propagation.{InstanceKey, PointerAnalysis}
import com.ibm.wala.ssa._
import com.ibm.wala.util.collections.HashSetMultiMap
import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigResolveOptions}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder

import scala.collection.JavaConverters._

abstract class IfdsTaintAnalysis(configPath: String) extends IfdsProblem with VariableFacts with SecretDefinition {

  private[this] val config =
    ConfigFactory.load(
      configPath,
      ConfigParseOptions.defaults.setAllowMissing(false),
      ConfigResolveOptions.defaults)

  val builder = FlexibleCallGraphBuilder()(config)

  override val callGraph: CallGraph = builder.cg
  override val supergraph: ISupergraph[Node, Procedure] = ICFGSupergraph.make(callGraph, builder._cache)
  override val entryPoints: Seq[NodeType] = callGraph.getEntrypointNodes.asScala.toSeq flatMap supergraph.getEntriesForProcedure map createNodeType
  override val pointerAnalysis: PointerAnalysis[InstanceKey] =
    builder match {
      case b: FlexibleCallGraphBuilder =>
        b.getPointerAnalysis
    }

  override def getValNum(factElem: ValueNumber, node: XNode): ValueNumber = factElem

  override type FactElem = ValueNumber
  override val O: Fact   = Λ

  /**
   * Do we consider the String[] args array of a main method to contain secret strings?
   * This assumes that the entry points of a program contain a main(String[] args) method.
   */
  private[this] def isSecretMainArgsArray(arrayRef: ValueNumber, node: Node): Boolean =
    mainArgsSecret && (entryPoints exists {
      ep =>
        enclProc(ep.node) == enclProc(node)
    }) && arrayRef == 1

  override def ifdsOtherSuccEdges: IfdsOtherEdgeFn =
    ideN1 => {
      val n1            = ideN1.n
      val d1            = ideN1.d
      val defaultResult = Set(d1)
      val method        = n1.node.getMethod
      n1.node.getLastInstruction match {
        case returnInstr: SSAReturnInstruction if hasRetValue(returnInstr)          =>
          d1 match {
            // we are returning a secret value, because an existing (i.e. secret) fact d1 is returned
            case v@Variable(m, _)
              if isFactReturned(v, n1, returnInstr.getResult) =>
                (methodToReturnVars get m).asScala.toSet ++ (if (m == method) Set.empty[Fact] else defaultResult)
            case _                                            =>
              defaultResult
          }
        // Arrays
        case storeInstr: SSAArrayStoreInstruction
          if factSameAsVar(d1, method, storeInstr.getValue)                         =>
            defaultResult + ArrayElement
        case loadInstr: SSAArrayLoadInstruction if d1 == ArrayElement               =>
          val inference = getTypeInference(enclProc(n1.node))
          if (isSecretArrayElementType(inference.getType(loadInstr.getDef).getTypeReference))
            defaultResult + Variable(method, loadInstr.getDef)
          else
            defaultResult
        case loadInstr: SSAArrayLoadInstruction if isSecretMainArgsArray(loadInstr.getArrayRef, n1.node) =>
            defaultResult + ArrayElement + Variable(method, loadInstr.getDef)
        //  Fields
        case putInstr: SSAPutInstruction
          if factSameAsVar(d1, method, putInstr.getVal) ||
             isConcatClass(getTypeInference(enclProc(n1.node)).getType(putInstr.getVal)) =>
            defaultResult + Field(getIField(method.getClassHierarchy, putInstr.getDeclaredField))
        case getInstr: SSAGetInstruction                                            =>
          d1 match {
            case Field(field)
              if field == getIField(method.getClassHierarchy, getInstr.getDeclaredField) =>
                defaultResult + Variable(method, getInstr.getDef)
            case _                                                                       =>
              defaultResult
          }
        // Casts
        case castInstr: SSACheckCastInstruction
          if factSameAsVar(d1, method, castInstr.getVal)                            =>
            defaultResult + Variable(method, castInstr.getDef)
        case _                                                                      =>
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
    (ideN1, n2) =>
      ideN1.d match {
        case v@Variable(method, vn)
          if getParameterNumber(ideN1).isDefined &&
             isConcatClass(getTypeInference(enclProc(ideN1.n.node)).getType(vn)) =>
          // We passed a StringBuilder/StringBuffer as a parameter to the enclosing method;
          // the current fact ideN1.d corresponds to this parameter. We need to make sure
          // that the StringBuilder/buffer in the calling method becomes secret.
            getCallInstructions(ideN1.n, n2).toSet map {
              callInstr: SSAInvokeInstruction =>
                Variable(n2.node.getMethod, getValNumFromParameterNum(callInstr, getParameterNumber(ideN1).get))
            }
        case Variable(method, _) if method == ideN1.n.node.getMethod             =>
          Set.empty
        case _                                                                   =>
          Set(ideN1.d)
      }


  /**
   * Functions for edges to phi instructions
   */
  override def ifdsOtherSuccEdgesPhi: IfdsOtherEdgeFn =
    ideN1 => {
      val d1 = ideN1.d
      val n1 = ideN1.n.node
      ideN1.d match {
        case Variable(m, vn) => // we don't need to check that m == n1.getMethod, because local variables are removed at the end-return edge.
          val facts: Seq[Variable] = phiInstructions(n1) flatMap {
            phiInstr =>
              0 to phiInstr.getNumberOfUses - 1 find {
                phiInstr.getUse(_) == vn
              } match {
                case Some(_) =>
                  Set(Variable(m, phiInstr.getDef))
                case None =>
                  Set.empty[Variable]
              }
          }
          facts.toSet + d1
        case _                                    =>
          Set(d1)
      }
    }

  override def ifdsCallReturnEdges: IfdsEdgeFn =
    (ideN1, _) => {
      val d1 = ideN1.d
      val default = Set(d1)
      val n1 = ideN1.n.node
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction => // todo this method is hard to reason about and needs refactoring.
          val method = n1.getMethod
          val valNum = callValNum(callInstr)
          lazy val defaultPlusVar = if (valNum.isDefined) default + Variable(method, valNum.get) else default
          val value = if (callInstr.getNumberOfReturnValues == 0) None else Some(callInstr.getReturnValue(0))
          getOperationType(callInstr.getDeclaredTarget, n1.getNode, value) match {
            case Some(SecretLibraryCall)             =>
              defaultPlusVar
            case Some(ReturnsStaticSecretOrPreservesSecret)
              if callInstr.isStatic && d1 == Λ       =>
                defaultPlusVar
            case Some(SecretIfSecretArgument)
              if hasSecretArgument(d1, method, callInstr)    =>
                defaultPlusVar
            case Some(opType) if !callInstr.isStatic =>
              val receiver = callInstr.getReceiver
              val factEqReceiver = factSameAsVar(d1, method, receiver)
              opType match {
                case ReturnsStaticSecretOrPreservesSecret if factEqReceiver                      =>
                  defaultPlusVar
                case ReturnsSecretArray if factEqReceiver                      =>
                  default + ArrayElement
                case ConcatenatesStrings
                  if factEqReceiver || isSecondArgument(d1, method, callInstr) =>
                    defaultPlusVar + Variable(method, callInstr.getReceiver)
                case StringConcatConstructor
                  if isSecondArgument(d1, method, callInstr)                   =>
                    default + Variable(method, callInstr.getUse(0))
                case _                                                         =>
                  default
              }
            case _                                   =>
              default
          }
      }
    }

  private[this] def isSecondArgument(d1: Fact, method: IMethod, callInstr: SSAInvokeInstruction): Boolean =
    callInstr.getNumberOfUses >= 2 && factSameAsVar(d1, method, callInstr.getUse(1))
  
  private[this] def hasSecretArgument(d: Fact, method: IMethod, callInstr: SSAInvokeInstruction): Boolean =
    firstParameter(callInstr) to callInstr.getNumberOfParameters - 1 exists {
      argNum =>
        factSameAsVar(d, method, getValNumFromParameterNum(callInstr, argNum))
    }

  override def ifdsCallStartEdges: IfdsEdgeFn =
    (ideN1, n2) => {
      val n1            = ideN1.n.node
      val d1            = ideN1.d
      val targetMethod  = n2.node.getMethod
      val callerMethod  = n1.getMethod
      val defaultResult = d1 match {
        case Variable(`callerMethod`, _) =>
          Set.empty[Fact]
        case _                           =>
          Set(d1)
      }
      n1.getLastInstruction match {
        case callInstr: SSAInvokeInstruction =>
          if (isSecret(targetMethod) && d1 == Λ) {
            val valNum = callValNum(callInstr).get
            defaultResult + Variable(callerMethod, valNum)
          } else if (isConcatConstructor(targetMethod) && d1 == Λ){
            val valNum = initValNum(targetMethod, callInstr)
            val phis = getPhis(n1, valNum, callerMethod)
            defaultResult ++ phis
          } else if (exclude(n1, callInstr))
            Set.empty
          else
            getParameterNumber(ideN1, callInstr) match { // checks if we are passing d1 as an argument to the function
              case Some(argNum)                                       =>
                val substituteFact = Variable(targetMethod, getValNumFromParameterNum(n2.node, argNum))
                Set(substituteFact)
              case None if d1 == Λ && callValNum(callInstr).isDefined =>
                methodToReturnVars.put(targetMethod, Variable(callerMethod, callValNum(callInstr).get))
                defaultResult
              case None                                               =>
                defaultResult
            }
      }
    }

  private[this] def isConcatConstructor(method: IMethod): Boolean =
    method.isInit && isConcatClass(new PointType(method.getDeclaringClass))

  /**
   * Should the call instruction be excluded from the analysis?
   */
  private[this] def exclude(node: Node, callInstr: SSAInvokeInstruction): Boolean = {
    val vn = if (callInstr.getNumberOfReturnValues == 0) None else Some(callInstr.getReturnValue(0))
    def operationType = getOperationType(callInstr.getDeclaredTarget, node.getNode, vn)
    def ops: Set[SecretOperation] = Set(ConcatenatesStrings, NonSecretLibraryCall, SecretLibraryCall)
    invokedOnSecretClass(callInstr) ||
      operationType.isDefined && (ops contains operationType.get)
  }
  
  private[this] def invokedOnSecretClass(callInstr: SSAInvokeInstruction): Boolean =
    secretTypes contains callInstr.getDeclaredTarget.getDeclaringClass.getName.toString

  /**
   * Returns the Variables corresponding to a phi instruction.
   */
  private[this] def getPhis(node: Node, valNum: ValueNumber, method: IMethod): Set[Fact] =
    (enclProc(node).getIR.iteratePhis.asScala collect {
      case phiInstr if phiInstr.getUse(0) == valNum || phiInstr.getUse(1) == valNum =>
        val phi = Variable(method, phiInstr.getDef)
        val args = Set(Variable(method, phiInstr.getUse(0)), Variable(method, phiInstr.getUse(1)))
        args + phi
    }).flatten.toSet[Fact]

  private[this] val methodToReturnVars = new HashSetMultiMap[IMethod, Variable]

  private[this] def isFactReturned(d: Variable, n: NodeType, retVal: ValueNumber): Boolean =
    d.elem == retVal && d.method == n.node.getMethod
}
