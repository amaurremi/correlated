package ca.uwaterloo.ide.analysis

import com.ibm.wala.ssa.{SSAReturnInstruction, SSAArrayStoreInstruction, SSAInstruction}
import ca.uwaterloo.ide.{IdeSolver, IdeProblem}
import com.ibm.wala.classLoader.IMethod
import scala.collection.breakOut

trait PropagationTester extends VariableFacts { this: IdeProblem with IdeSolver =>

  /**
   * Let A be the set of all the assignment instruction nodes in the input program.
   * This method returns the LatticeVal elements corresponding to the node.
   * @param inMain If true, considers only assignment instructions inside of the main method.
   *               Otherwise, considers only assignment instructions outside of the main method.
   *               There is no option to consider all assignment statements in the program.
   * @param nonLambda If true, does not include Λ facts. Otherwise, considers only Λ facts.
   * @param expectedNumber The expected number of assignment statements returned by this method.
   */
  def getValsAtAssignments(
    inMain: Boolean,
    nonLambda: Boolean = true,
    expectedNumber: Int = 1
  ): Iterable[(Fact, LatticeElem)] =
    getInstructionVals(ArrayAssignment, inMain, nonLambda, expectedNumber)

  /**
   * Analogous to getAssignmentVals, but for return (instead of assignment) instructions.
   */
  def getValsAtReturn(
    inMain: Boolean,
    nonLambda: Boolean = true,
    expectedNumber: Int = 1
  ): Iterable[(VariableFact, LatticeElem)] =
    getInstructionVals(Return, inMain, nonLambda, expectedNumber)

  /**
   * Returns lattice element corresponding to a value returned by the function outside of main.
   * This method assumes there is only one function outside of main and that it returns exactly one value
   * in all execution paths.
   */
  def getReturnVal: (ValueNumber, IMethod) = {
    val (retVal, node) = (solvedResult.keys collectFirst {
      case n if !isInMainMethod(n) && n.n.getLastInstruction.isInstanceOf[SSAReturnInstruction] => // super ugly
        n.n.getLastInstruction.asInstanceOf[SSAReturnInstruction].getResult -> n.n
    }).get
    retVal -> node.getMethod
  }

  private[this] def getInstruction(node: IdeNode): SSAInstruction = node.n.getLastInstruction

  private[this] def nonLambdaFact(node: IdeNode): Boolean = node.d != Lambda

  private[this] def isInMainMethod(node: IdeNode): Boolean =
    entryPoints exists { enclProc(_) == enclProc(node.n) }

  private[this] def getInstructionVals(
    instr: InstructionType,
    inMain: Boolean,
    nonLambda: Boolean,
    expectedNumber: Int
  ): Iterable[(Fact, LatticeElem)] = {
    val isCorrectInstruction = instr.doesMatch
    val instructionVals: Iterable[(Fact, LatticeElem)] = (solvedResult collect {
      case (node, value)
        if isCorrectInstruction(node) && (nonLambda == nonLambdaFact(node) && (inMain == isInMainMethod(node))) =>
          node.d -> value
    })(breakOut)
    assertExpectedInstructionNumber(inMain, expectedNumber, instructionVals, instr)
    instructionVals
  }

  private[this] def assertExpectedInstructionNumber(inMain: Boolean, expectedNumber: Int, instructionVals: Iterable[(Fact, LatticeElem)], instr: InstructionType) {
    val inOrOutside = if (inMain) "inside" else "outside"
    val (verb, plural) = if (expectedNumber == 1) ("is ", " ") else ("are ", "s ")
    val size = instructionVals.size
    assert(size == expectedNumber, "There " + verb + expectedNumber + " " + instr.instrName + plural + inOrOutside + " the main method, and not " + size + " as returned")
  }

  def onlyLatticeElem: ((Fact, LatticeElem)) => LatticeElem = _._2

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

  private[this] case object Return extends InstructionType {

    override def instrName = "return statement"

    override def doesMatch =
      node =>
        getInstruction(node) match {
          case n: SSAReturnInstruction => true
          case _                       => false
        }
  }
}
