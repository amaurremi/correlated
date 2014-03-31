package ca.uwaterloo.ide.example.cp

import collection.mutable
import com.ibm.wala.ssa._
import com.ibm.wala.types.MethodReference
import scala.collection.JavaConverters._

class CopyConstantPropagation(fileName: String) extends ConstantPropagation(fileName) with ConstantPropagationTester {

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
  // JavaLanguage$JavaInstructionFactory$14, 15 <=> SSAPutInstruction         <- assignment
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
        case assignment: SSAPutInstruction        =>
          edgesForAssignment(assignment, n2, d1)
        case assignment: SSAArrayStoreInstruction =>
          edgesForAssignment(assignment, n2, d1)
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
          edgesForCallAssignment(assignment, ideN1, n2)
        case _                                    =>
          Set(FactFunPair(ideN1.d, Id))
      }
    }

  private[this] def edgesForCallAssignment(
    assignment: SSAArrayStoreInstruction,
    ideN1: IdeNode,
    n2: Node
  ): Set[FactFunPair] = {
    val n1 = ideN1.n
    val d1 = ideN1.d
    val idFactFunPairSet = Set(FactFunPair(d1, Id))
    val ir = enclProc(n1).getIR
    val returnValue: Option[ValueNumber] = getSingleReturnValue(ir)
    val symbolTable = ir.getSymbolTable // todo what if we return something like a function parameter (which is not in the symbol table)?
    if (d1 == Λ)
      idFactFunPairSet +
        FactFunPair(
          SomeFact(n2.getMethod.getReference, getLVar(assignment, n2)),
          returnValue match {
            case Some(retVal) if symbolTable isConstant retVal =>
              CpFunction(Num(retVal, n1.getMethod.getReference))
            case _                                             =>
              Id
          }
        )
    else idFactFunPairSet
  }

  private[this] def getSingleReturnValue(ir: IR): Option[ValueNumber] = {
    val returns: Set[ValueNumber] = (ir.iterateAllInstructions().asScala collect { // todo is this correct??? THIS IS IMPORTANT SINCE IT STRONGLY DIFFERS FROM THE PAPER
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
      val callInstr = ideN1.n.getLastInstruction.asInstanceOf[SSAInvokeInstruction] // todo match doesn't work
      getParameterNumber(ideN1.d, callInstr) match {
        case Some(argNum) => // checks if we are passing d1 as an argument to the function
          val targetFact = getArrayElemFromParameterNum(n2, argNum)
          Set(FactFunPair(targetFact, Id))
        case None         =>
          Set(FactFunPair(ideN1.d, Id))
      }
    }

  private[this] def updateArrayElementValNums(assignment: SSAArrayStoreInstruction, n: Node) = {
    val arrayRef = assignment.getArrayRef
    val arrayInd = assignment.getIndex
    enclProc(n).getIR.iterateNormalInstructions().asScala collectFirst { // todo inefficient
      case instruction: SSAArrayLoadInstruction
        if instruction.getArrayRef == arrayRef && instruction.getIndex == arrayInd =>
          val method: MethodReference = n.getMethod.getReference
          val fact = SomeFact(method, ArrayElement(arrayRef, arrayInd))
          val valNum: Int = instruction.getDef
          valNumsToArrayElems += (valNum, method) -> fact
          arrayElemsToValNums += fact -> valNum
    }
  }

  private[this] def getLVar(instr: SSAInstruction, n: Node): ArrayElement =
    instr match {
      case assignment: SSAArrayStoreInstruction =>
        updateArrayElementValNums(assignment, n)
        ArrayElement(assignment.getArrayRef, assignment.getIndex)
      case _                                    =>
        throw new IllegalArgumentException("lvar retrieval on non-assignment statement " + instr.toString)
    }

  private[this] val valNumsToArrayElems = mutable.Map[(ValueNumber, MethodReference), CpFact]()
  private[this] val arrayElemsToValNums = mutable.Map[CpFact, ValueNumber]()

  private[this] def getRVal(instr: SSAInstruction): ValueNumber =
    instr match {
      case assignment: SSAPutInstruction        =>
        assignment.getVal
      case assignment: SSAArrayStoreInstruction =>
        assignment.getValue
      case _                                    =>
        throw new IllegalArgumentException("rval retrieval on non-assignment statement " + instr.toString)
    }

  private[this] def edgesForAssignment(
    assignment: SSAInstruction,
    n2: Node,
    d1: Fact
  ): Set[FactFunPair] = {
    val assignedVal = getRVal(assignment)
    val symbolTable = enclProc(n2).getIR.getSymbolTable
    val idFactFunPairSet = Set(FactFunPair(d1, Id))
    if (d1 == Λ)
      idFactFunPairSet +
        FactFunPair(
          SomeFact(n2.getMethod.getReference, getLVar(assignment, n2)),
          if (symbolTable isConstant assignedVal)
            CpFunction(Num(assignedVal, n2.getMethod.getReference))
          else Id
        )
    else idFactFunPairSet
  }

  private[this] def idEdges: EdgeFn =
    (ideN1, _) =>
      Set(FactFunPair(ideN1.d, Id))

  private[this] def getArrayElemFromParameterNum(n: Node, argNum: Int): CpFact = {
    val valNumber = enclProc(n).getIR.getSymbolTable.getParameter(argNum)
    val method: MethodReference = n.getMethod.getReference
    SomeFact(method, ElemInTargetMethod(valNumber))
  }

  /**
   * If the variable corresponding to this fact is passed as a parameter to this call instruction,
   * returns the number of the parameter.
   */
  private[this] def getParameterNumber(fact: Fact, callInstr: SSAInvokeInstruction): Option[Int] =
    fact match {
      case f: SomeFact =>
        val valNum = arrayElemsToValNums(f)
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

  case class Num(n: ValueNumber, method: MethodReference) extends CpLatticeElem {
    override def ⊓(ln: CpLatticeElem) = ln match {
      case Num(n2, method2) => if (n == n2 && method == method2) ln else ⊥
      case _       => ln ⊓ this
    }
    override def toString: String = "variable value " + n.toString + " in " + method.getName.toString + "()"
  }
}