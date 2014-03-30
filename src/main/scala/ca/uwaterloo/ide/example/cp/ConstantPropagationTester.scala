package ca.uwaterloo.ide.example.cp

import com.ibm.wala.ssa.{SSAReturnInstruction, SSAArrayStoreInstruction, SSAInstruction}

trait ConstantPropagationTester { this: ConstantPropagation =>

  /**
   * Let A be the set of all the assignment instruction nodes in the input program.
   * This method returns the LatticeVal elements corresponding to the node.
   * @param inMain If true, considers only assignment instructions inside of the main method.
   *               Otherwise, considers only assignment instructions outside of the main method.
   *               There is no option to consider all assignment statements in the program.
   * @param nonLambda If true, does not consider Λ facts. Otherwise, considers only Λ facts.
   * @param expectedNumber The expected number of assignment statements returned by this method.
   */
  def getAssignmentVals(
    inMain: Boolean, 
    nonLambda: Boolean = true, 
    expectedNumber: Int = 1
  ): Iterable[LatticeElem] =
    getInstructionVals(ArrayAssignment, inMain, nonLambda, expectedNumber)

  /**
   * Analogous to getAssignmentVals, but for return instead of assignment instructions.
   */
  def getReturnVals(
    inMain: Boolean, 
    nonLambda: Boolean = true, 
    expectedNumber: Int = 1
  ): Iterable[LatticeElem] =
    getInstructionVals(Return, inMain, nonLambda, expectedNumber)

  private[this] def getInstruction(node: IdeNode): SSAInstruction = node.n.getLastInstruction

  private[this] def nonLambdaFact(node: IdeNode): Boolean = node.d != Lambda

  private[this] def isInMainMethod(node: IdeNode): Boolean =
    entryPoints exists { enclProc(_) == enclProc(node.n) }

  private[this] def getInstructionVals(
    instr: InstructionType, 
    inMain: Boolean, 
    nonLambda: Boolean,
    expectedNumber: Int
  ): Iterable[LatticeElem] = {
    val isCorrectInstruction = instr.doesMatch
    val instructionVals = solvedResult collect {
      case (node, value)
        if isCorrectInstruction(node) && (nonLambda == nonLambdaFact(node) && (inMain == isInMainMethod(node))) =>
          value
    }
    val inOrOutside = if (inMain) "inside" else "outside"
    val (verb, plural) = if (expectedNumber == 1) ("is ", " ") else ("are ", "s ")
    val size = instructionVals.size
    assert(size == expectedNumber, "There " + verb + expectedNumber + " " + instr.instrName + plural + inOrOutside + " the main method, and not " + size)
    instructionVals
  }

  private[this] trait InstructionType {

    def instrName: String

    def doesMatch: IdeNode => Boolean
  }

  private[this] case object ArrayAssignment extends InstructionType {

    override def instrName = "assignment"

    override def doesMatch =
      node =>
        getInstruction(node) match {
          case n: SSAArrayStoreInstruction => true
          case _                           => false
        }
  }

  private[this] case object Return extends InstructionType{

    override def instrName = "return statement"

    override def doesMatch =
      node =>
        getInstruction(node) match {
          case n: SSAReturnInstruction => true
          case _                       => false
        }
  }
}
