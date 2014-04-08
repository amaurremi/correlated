package ca.uwaterloo.ide

import ca.uwaterloo.ide.analysis.VariableFacts
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.{SSAInstruction, SSAReturnInstruction}
import scala.Iterable
import scala.collection._

trait PropagationSpecBuilder extends VariableFacts { this: IdeProblem with IdeSolver =>

  /**
   * Let A be the set of all the assignment instruction nodes in the input program.
   * This method returns the LatticeVal elements corresponding to the node.
   * @param inMain If true, considers only assignment instructions inside of the main method.
   *               Otherwise, considers only assignment instructions outside of the main method.
   *               There is no option to consider all assignment statements in the program.
   * @param nonLambda If true, does not include Λ facts. Otherwise, considers only Λ facts.
   * @param expectedNumber The expected number of assignment statements returned by this method.
   */
  def getVarsAtAssignments(
    inMain: Boolean,
    nonLambda: Boolean = true,
    expectedNumber: Int = 1
  ): Iterable[(Fact, LatticeElem)] =
    getInstructionVars(Assignment, inMain, nonLambda, expectedNumber)

  /**
   * Analogous to getAssignmentVals, but for return (instead of assignment) instructions.
   */
  def getVarsAtReturn(
    inMain: Boolean,
    nonLambda: Boolean = true,
    expectedNumber: Int = 1
  ): Iterable[(VariableFact, LatticeElem)] =
    getInstructionVars(Return, inMain, nonLambda, expectedNumber)

  /**
   * Analogous to getAssignmentVals, but for return (instead of assignment) instructions.
   */
  def getVarsAtSecretAssignment(
    inMain: Boolean,
    nonLambda: Boolean = true,
    expectedNumber: Int = 1
  ): Iterable[(VariableFact, LatticeElem)] =
    getInstructionVars(Return, inMain, nonLambda, expectedNumber)

  def filterByOrigin(variables: Iterable[(VariableFact, LatticeElem)], inMain: Boolean): Iterable[(VariableFact, LatticeElem)] =
    variables filter {
      case (Variable(method, el), _) =>
        val filterFunction = if (inMain) entryPoints.exists _ else entryPoints.forall _
        filterFunction {
          ep =>
            (ep.getMethod == method) == inMain
        }
      case _                         =>
        false
    }

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

  protected def getInstruction(node: IdeNode): SSAInstruction = node.n.getLastInstruction

  private[this] def nonLambdaFact(node: IdeNode): Boolean = node.d != Lambda

  private[this] def isInMainMethod(node: IdeNode): Boolean =
    entryPoints exists { enclProc(_) == enclProc(node.n) }

  private[this] def getInstructionVars(
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

  protected trait InstructionType {

    def instrName: String

    def doesMatch: IdeNode => Boolean
  }

  /**
   * The type of "significant" assignment for the analysis. E.g. in our implementation of constant propagation,
   * it's array assignment, because we only deal with arrays. In taint analysis, it's the assignment of the
   * value returned by the secret() method.
   */
  val Assignment: InstructionType

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
