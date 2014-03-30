package ca.uwaterloo.ide.example.cp

import com.ibm.wala.ssa.{SSAReturnInstruction, SSAArrayStoreInstruction, SSAInstruction}

trait ConstantPropagationTester { this: ConstantPropagation =>

  def getInstruction(node: IdeNode): SSAInstruction = node.n.getLastInstruction
 
  def isArrayAssignment(node: IdeNode): Boolean =
    getInstruction(node) match {
      case n: SSAArrayStoreInstruction => true
      case _                           => false
    }
  
  def isReturn(node: IdeNode): Boolean =
    getInstruction(node) match {
      case n: SSAReturnInstruction => true
      case _                       => false  
    }
  
  def nonLambdaFact(node: IdeNode): Boolean = node.d != Lambda

  def isInMainMethod(node: IdeNode): Boolean =
    entryPoints exists { enclProc(_) == enclProc(node.n) }

  def getAssignmentVals(inMain: Boolean, nonLambda: Boolean = true, expectedNumber: Int = 1) =
    getInstructionVals(ArrayAssignment, inMain, nonLambda, expectedNumber)

  def getReturnVals(inMain: Boolean, nonLambda: Boolean = true, expectedNumber: Int = 1) =
    getInstructionVals(ReturnStatement, inMain, nonLambda, expectedNumber)

  private[this] def getInstructionVals(instr: InstructionType, inMain: Boolean, nonLambda: Boolean, expectedNumber: Int) = {
    val isCorrectInstruction = instr match {
      case ArrayAssignment => isArrayAssignment _
      case ReturnStatement => isReturn _
    }
    val instructionVals = solvedResult collect {
      case (node, value)
        if isCorrectInstruction(node) && (nonLambda == nonLambdaFact(node) && (inMain == isInMainMethod(node))) =>
          value
    }
    val inOrOutside = if (inMain) "inside" else "outside"
    assert(instructionVals.size == expectedNumber, "There is (are) " + expectedNumber + " " + instr.toString + "(s) " + inOrOutside + " the main method")
    instructionVals
  }

  private[this] trait InstructionType {
    def name: String
  }

  private[this] case object ArrayAssignment extends InstructionType {
    override def name: String = "assignment"
  }

  private[this] case object ReturnStatement extends InstructionType{
    override def name: String = "return statement"
  }
}
