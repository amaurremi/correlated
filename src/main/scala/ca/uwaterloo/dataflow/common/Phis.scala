package ca.uwaterloo.dataflow.common

trait Phis { this: SuperGraphTypes =>

  type PhiInstruction

  def phiInstructions(node: Node): Seq[PhiInstruction]
}
