package ca.uwaterloo.ide.cp

import ca.uwaterloo.ide.PropagationSpecBuilder
import ca.uwaterloo.ide.analysis.cp.CopyConstantPropagation
import com.ibm.wala.ssa.SSAArrayStoreInstruction

class CopyConstantPropagationSpecBuilder(fileName: String) extends CopyConstantPropagation(fileName) with PropagationSpecBuilder
