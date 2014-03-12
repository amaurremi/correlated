package ca.uwaterloo.ide.example.cp

import com.ibm.wala.ssa.{SSAInstruction, SSAArrayStoreInstruction, SSAPutInstruction}

class CopyConstantPropagation(fileName: String) extends ConstantPropagation(fileName) {

  override type IdeFunction = CpFunction
  override type LatticeElem = CpLatticeElem

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = CpFunction(⊤, l = true)
  override val λTop: IdeFunction   = CpFunction(⊤)

  // todo remove
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
      val idFactFunPairSet = Set(FactFunPair(d1, Id))
      n2.getLastInstruction match {
        case assignment: SSAPutInstruction           =>
          edgesForAssignment(assignment, assignment.getVal, n2, d1, idFactFunPairSet)
        case assignment: SSAArrayStoreInstruction    =>
          edgesForAssignment(assignment, assignment.getValue, n2, d1, idFactFunPairSet)
        case x                                       =>
          idFactFunPairSet
      }
    }

  def edgesForAssignment(
    assignment: SSAInstruction,
    assignedVal: Int,
    n2: Node,
    d1: Fact,
    idFactFunPairSet: Set[FactFunPair]
  ): Set[FactFunPair] = {
    val symbolTable = (supergraph getProcOf n2).getIR.getSymbolTable
    if (symbolTable isConstant assignedVal) {
      if (d1 == Λ)
        idFactFunPairSet + FactFunPair(CpFact(Some(assignment)), CpFunction(Num(assignedVal)))
      else Set.empty
    } else idFactFunPairSet
  }

  /**
   * Functions for inter-procedural edges from an end node to the return node of the callee function.
   */
  override def endReturnEdges: EdgeFn =
    callStartEdges // todo

  /**
   * Functions for intra-procedural edges from a call to the corresponding return edges.
   */
  override def callReturnEdges: EdgeFn =
    (ideN1, _) =>
      Set(FactFunPair(ideN1.d, Id)) // todo not for fields/static variables

  /**
   * Functions for inter-procedural edges from a call node to the corresponding start edges.
   */
  override def callStartEdges: EdgeFn =
    (ideN1, n2) =>
      if (ideN1.d == Λ)
        Set(FactFunPair(Λ, Id))
      else Set.empty
    // todo substitution for parameters. For now, we assume functions don't take parameters and that there are no fields/static variables

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
      if (l) CpFunction(c ⊓ f.c, l)
      else this
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

  case class Num(n: Long) extends CpLatticeElem {
    override def ⊓(ln: CpLatticeElem) = ln match {
      case Num(n2) => if (n == n2) ln else ⊥
      case _       => ln ⊓ this
    }
    override def toString: String = n.toString
  }
}