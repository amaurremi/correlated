package ca.uwaterloo.ide.example.cp

import com.ibm.wala.ssa.{SSAStoreIndirectInstruction, SSAPutInstruction}

class CopyConstantPropagation(fileName: String) extends ConstantPropagation(fileName) {

  override type IdeFunction = CpFunction
  override type LatticeElem = CpLatticeElem

  override val Bottom: LatticeElem = ⊥
  override val Top: LatticeElem    = ⊤
  override val Id: IdeFunction     = CpFunction(⊤)
  override val λTop: IdeFunction   = CpFunction(⊤) // todo correct?

  /**
   * Functions for all other (inter-procedural) edges.
   */
  override def otherSuccEdges: EdgeFn =
    (ideN1, n2) => {
      val d1 = ideN1.d
      val idFactFunPairSet = Set(FactFunPair(d1, Id))
      n2.getLastInstruction match {
        case assignment: SSAPutInstruction           =>
          edgesForAssignment(assignment, n2, d1, idFactFunPairSet)
        case assignment: SSAStoreIndirectInstruction =>
          ???
        case _                                       =>
          idFactFunPairSet
      }
    }


  def edgesForAssignment(
    assignment: SSAPutInstruction,
    n2: Node,
    d1: Fact,
    idFactFunPairSet: Set[FactFunPair]
  ): Set[FactFunPair] = {
    val assignedVal = assignment.getVal
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
   * λl . (a * l + b) ⊓ c
   * as described on p. 153 of Sagiv, Reps, Horwitz, "Precise inter-procedural dataflow analysis
   * with applications to constant propagation"
   */
  case class CpFunction(c: LatticeElem) extends IdeFunctionI {

    override def apply(arg: LatticeElem): LatticeElem = arg ⊓ c

    /**
     * Meet operator
     */
    override def ⊓(f: CpFunction): CpFunction = CpFunction(c ⊓ f.c)

    override def ◦(f: CpFunction): CpFunction = ⊓(f) // todo correct?
  }

  trait CpLatticeElem extends Lattice {
    def ⊓(n: CpLatticeElem): CpLatticeElem
  }

  case object ⊤ extends CpLatticeElem {
    override def ⊓(n: CpLatticeElem) = n
  }

  case object ⊥ extends CpLatticeElem {
    override def ⊓(n: CpLatticeElem) = ⊥
  }

  case class Num(n: Long) extends CpLatticeElem {
    override def ⊓(ln: CpLatticeElem) = ln match {
      case Num(n2) => if (n == n2) ln else ⊥
      case _       => ln ⊓ this
    }
  }
}
