package ca.uwaterloo.ide.example.cp

import collection.mutable
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa._
import scala.collection.JavaConverters._

class CopyConstantPropagation(fileName: String) extends ConstantPropagation(fileName) {

  override type IdeFunction = CpFunction
  override type LatticeElem = CpLatticeElem

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = CpFunction(⊤, l = true)
  override val λTop: IdeFunction   = CpFunction(⊤)
  
  private[this] type ValueNumber = Int

  // javap -c -cp <jar name or dir name> <class file name>
  // JavaLanguage$JavaInstructionFactory$1      <=> SSAArrayLengthInstruction
  // JavaLanguage$JavaInstructionFactory$2      <=> SSAArrayLoadInstruction
  // JavaLanguage$JavaInstructionFactory$3      <=> SSAArrayStoreInstruction  <- assignment
  // JavaLanguage$JavaInstructionFactory$4      <=> SSABinaryOpInstruction
  // JavaLanguage$JavaInstructionFactory$5      <=> SSACheckCastInstruction
  // JavaLanguage$JavaInstructionFactory$6      <=> SSAConversionInstruction
  // JavaLanguage$JavaInstructionFactory$7, 8   <=> SSAGetInstruction
  // JavaLanguage$JavaInstructionFactory$9, 10  <=> SSAInvokeInstruction
  // JavaLanguage$JavaInstructionFactory$11     <=> SSAMonitorInstruction
  // JavaLanguage$JavaInstructionFactory$12     <=> SSANewInstruction
  // JavaLanguage$JavaInstructionFactory$13     <=> SSAPhiInstruction
  // JavaLanguage$JavaInstructionFactory$14, 15 <=> SSAPutInstruction         <- assignment (but we don't handle that type)
  // JavaLanguage$JavaInstructionFactory$16     <=> SSAThrowInstruction
  // JavaLanguage$JavaInstructionFactory$17     <=> SSALoadMetadataInstruction
  // JavaLanguage$JavaInstructionFactory$18     <=> SSANewInstruction

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn =
    (ideN1, n2) => {
      val d1 = ideN1.d
      n2.getLastInstruction match {
        case assignment: SSAArrayStoreInstruction =>
          edgesForAssignment(n2, d1)
        case _                                    =>
          Set(FactFunPair(d1, Id))
      }
    }

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: EdgeFn =
    (ideN1, n2) => {
      n2.getLastInstruction match {
        case assignment: SSAArrayStoreInstruction =>
          edgesForCallAssignment(ideN1, n2)
        case _                                    =>
          Set(FactFunPair(ideN1.d, Id))
      }
    }

  private[this] def edgesForCallAssignment(
    ideN1: IdeNode,
    n2: Node
  ): Set[FactFunPair] = {
    val n1               = ideN1.n
    val d1               = ideN1.d
    val idFactFunPairSet = Set(FactFunPair(d1, Id))
    if (d1 == Λ)
      idFactFunPairSet +
        FactFunPair(
          SomeFact(n2.getMethod, getLVar(n2)),
          callAssignmentCpFunction(n1)
        )
    else getSingleReturnValue(n1) match {
      case Some(retVal)
        if retValSameAsFact(retVal, n1, d1) =>
          Set(FactFunPair(
            SomeFact(n2.getMethod, getLVar(n2)),
            Id))
      case _                                                       =>
        idFactFunPairSet
    }
  }

  private[this] def callAssignmentCpFunction(n: Node): IdeFunction = {
    val optReturnVal = getSingleReturnValue(n)
    optReturnVal match {
      case Some(retVal) if enclProc(n).getIR.getSymbolTable isConstant retVal =>
        CpFunction(Num(retVal, n.getMethod))
      case _ =>
        Id
    }
  }

  /**
   * If we have an assignment
   *   x = f()
   * we want to propagate the return value of f into x.
   * If the return statement in f returns a variable that is a constant, e.g.
   *   f { int y = 1; return y; }
   * then we create an edge from fact y to fact x with an Id function.
   * This method tests if the returned variable is the same as the fact of the source node.
   */
  private[this] def retValSameAsFact(retVal: ValueNumber, node: Node, fact: Fact): Boolean =
    getFactByValNum(fact, node) match {
      case Some(SomeFact(method2, ArrayElemByValNumber(valNum))) =>
        retVal == valNum && node.getMethod == method2
      case _                                                    =>
        false
    }

  private[this] def getFactByValNum(fact: Fact, node: Node): Option[SomeFact] = {
    updateAllArrayElementValNums(node)
    val method = node.getMethod
    fact match {
      case SomeFact(method2, el@ArrayElemByArrayAndIndex(_, _)) =>
        arrayElemsToValNums.get(el, method2) map {
          valNum =>
            SomeFact(method, ArrayElemByValNumber(valNum))
        }
      case sf@SomeFact(_, ArrayElemByValNumber(_)) =>
        Some(sf)
      case _ =>
        None
    }
  }

  private[this] def updateAllArrayElementValNums(node: Node) = // todo very inefficient
    allNodesInProc(node) foreach {
      n =>
        n.getLastInstruction match {
          case _: SSAArrayStoreInstruction =>
            updateArrayElementValNums(n)
          case instr: SSAArrayLoadInstruction  =>
            val method = node.getMethod
            val valNum = instr.getDef
            val elem   = ArrayElemByArrayAndIndex(instr.getArrayRef, instr.getIndex)
            updateArrayMaps(elem, valNum, method)
          case _                           => ()
        }
    }

  private[this] def updateArrayElementValNums(n: Node) = {
    val assignment = getAssignmentInstr(n)
    val arrayRef = assignment.getArrayRef
    val arrayInd = assignment.getIndex
    followingInstructions(n) collectFirst { // todo inefficient
      case instruction: SSAArrayLoadInstruction
        if instruction.getArrayRef == arrayRef && instruction.getIndex == arrayInd =>
        updateArrayMaps(ArrayElemByArrayAndIndex(arrayRef, arrayInd), instruction.getDef, n.getMethod)
    }
  }

  private[this] def updateArrayMaps(arrayElem: ArrayElemByArrayAndIndex, valNum: ValueNumber, method: IMethod) {
    arrayElemsToValNums += (arrayElem, method) -> valNum
    valNumsToArrayElems += (valNum, method) -> arrayElem
  }

  /**
   * If the node's enclosing procedure always returns the same value, returns that value.
   */
  private[this] def getSingleReturnValue(node: Node): Option[ValueNumber] = {
    val returns: Set[ValueNumber] = (instructionsInProc(node) collect { // this is an inefficient implementation. A better way is to keep track of return values in a method using CpFacts
      case instr: SSAReturnInstruction => instr.getResult
    }).toSet
    if (returns.size == 1) Some(returns.head)
    else None
  }

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: EdgeFn =
    idEdges // todo not for fields/static variables

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  // todo parameters need to be associated with arguments including the case where the argument was not assigned a value before (e.g. if it's an args[] element of the main method)
  override def callStartEdges: EdgeFn =
    (ideN1, n2) => {
      ideN1.n.getLastInstruction match {
        case callInstr: SSAInvokeInstruction =>
          getParameterNumber(ideN1, callInstr) match {
            case Some(argNum) => // checks if we are passing d1 as an argument to the function
              val targetFact = getArrayElemFromParameterNum(n2, argNum)
              Set(FactFunPair(targetFact, Id))
            case None         =>
              Set(FactFunPair(ideN1.d, Id))
          }
        case _ => throw new UnsupportedOperationException("callStartEdges invoked on non-call instruction")
      }
    }

  private[this] lazy val instructionsInProc =
    (node: Node) =>
      enclProc(node).getIR.iterateNormalInstructions().asScala

  private[this] def followingInstructions(n: Node): Iterator[SSAInstruction] =
    followingNodes(n) map { _.getLastInstruction }

  private[this] def getLVar(n: Node): ArrayElemByArrayAndIndex = {
    val assignment = getAssignmentInstr(n)
    ArrayElemByArrayAndIndex(assignment.getArrayRef, assignment.getIndex)
  }

  private[this] val valNumsToArrayElems = mutable.Map[(ValueNumber, IMethod), ArrayElemByArrayAndIndex]()
  private[this] val arrayElemsToValNums = mutable.Map[(ArrayElemByArrayAndIndex, IMethod), ValueNumber]()

  private[this] def getRVal(n: Node): ValueNumber = {
    val assignment = getAssignmentInstr(n)
    assignment.getValue
  }

  private[this] def getAssignmentInstr(node: Node): SSAArrayStoreInstruction =
    node.getLastInstruction match {
      case arrayAssignment: SSAArrayStoreInstruction =>
        arrayAssignment
      case _                                         =>
        throw new IllegalArgumentException("Currently, only array store assignments are handled")
    }

  private[this] def edgesForAssignment(
    n2: Node,
    d1: Fact
  ): Set[FactFunPair] = {
    val assignedVal      = getRVal(n2)
    val idFactFunPairSet = Set(FactFunPair(d1, Id))
    val fact = SomeFact(n2.getMethod, getLVar(n2))
    if (d1 == Λ)
      idFactFunPairSet +
        FactFunPair(
          fact,
          assignmentCpFunction(assignedVal, n2)
        )
    else if (factIsRVal(d1, n2))
      Set(FactFunPair(fact, Id))
    else idFactFunPairSet
  }

  /**
   * For a fact, checks whether the right-hand side of the assignment instruction in node 'n' is the value of the fact.
   * For example, for an assignment
   *   int x = a
   * this checks whether 'a' has the same value number as 'fact' (and corresponds to the same method).
   */
  private[this] def factIsRVal(fact: Fact, n: Node): Boolean = {
    val factByValNum = getFactByValNum(fact, n)
    factByValNum match {
      case Some(SomeFact(method, ArrayElemByValNumber(valNum))) =>
        getRVal(n) == valNum && method == n.getMethod
      case _                                                    =>
        false
    }
  }

  private[this] def assignmentCpFunction(
    assignedVal: ValueNumber,
    n2: Node
  ): IdeFunction =
    if (enclProc(n2).getIR.getSymbolTable isConstant assignedVal)
      CpFunction(Num(assignedVal, n2.getMethod))
    else if (isCall(assignedVal, n2))
      λTop
    else Id

  /**
   * Given the node whose instruction is an assignment, and
   * Returns true if the
   */
  private[this] def isCall(value: ValueNumber, node: Node): Boolean =
    instructionsInProc(node) exists {
      case instr: SSAInvokeInstruction
        if instr.getReturnValue(0) == value => true
      case _                                => false
    }

  private[this] def idEdges: EdgeFn =
    (ideN1, _) =>
      Set(FactFunPair(ideN1.d, Id))

  private[this] def getArrayElemFromParameterNum(n: Node, argNum: Int): CpFact = {
    val valNumber = enclProc(n).getIR.getSymbolTable.getParameter(argNum)
    SomeFact(n.getMethod, ArrayElemByValNumber(valNumber))
  }

  /**
   * If the variable corresponding to this fact is passed as a parameter to this call instruction,
   * returns the number of the parameter.
   */
  private[this] def getParameterNumber(node: IdeNode, callInstr: SSAInvokeInstruction): Option[Int] =
    node.d match {
      case SomeFact(method, elem) =>
        val valNum = elem match {
          case byRefInd: ArrayElemByArrayAndIndex =>
            updateAllArrayElementValNums(node.n)
            arrayElemsToValNums(byRefInd, method)
          case ArrayElemByValNumber(vn)           =>
            vn
        }
        0 to callInstr.getNumberOfParameters - 1 find { // todo starting with 0 because we're assuming it's a static method
          callInstr.getUse(_) == valNum
        }
      case Lambda      => None
    }

  /**
   * Represents a function
   * λl . l ⊓ c
   * @param l if true, the function is of type λl . l ⊓ c
   *          if false, it's a constant function of type λl . c
   */
  case class CpFunction(
    c: LatticeElem, 
    l: Boolean = false
  ) extends IdeFunctionI {

    override def apply(arg: LatticeElem): LatticeElem =
      if (l) c ⊓ arg
      else c

    override def ⊓(f: CpFunction): CpFunction =
      CpFunction(c ⊓ f.c, l || f.l)

    override def ◦(f: CpFunction): CpFunction =
      if (l) CpFunction(c ⊓ f.c, f.l)
      else this

    override def toString: String =
      if (l && c == ⊤)
        "id"
      else
        "λl . " +
          (if (l) "l  ⊓ " else "") +
          c.toString
  }

  /**
   * Represents lattice elements for the set L
   */
  trait CpLatticeElem extends Lattice {
    def ⊓(n: CpLatticeElem): CpLatticeElem
  }

  case object ⊤ extends CpLatticeElem {
    override def ⊓(n: CpLatticeElem) = n
    override def toString: String = "top"
  }

  case object ⊥ extends CpLatticeElem {
    override def ⊓(n: CpLatticeElem) = ⊥
    override def toString: String = "bottom"
  }

  case class Num(n: ValueNumber, method: IMethod) extends CpLatticeElem {
    override def ⊓(ln: CpLatticeElem) = ln match {
      case Num(n2, method2) => if (n == n2 && method == method2) ln else ⊥
      case _       => ln ⊓ this
    }
    override def toString: String = "variable value " + n.toString + " in " + method.getName.toString + "()"
  }
}