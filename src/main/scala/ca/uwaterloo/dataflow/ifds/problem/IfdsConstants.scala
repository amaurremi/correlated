package ca.uwaterloo.dataflow.ifds

import ca.uwaterloo.dataflow.common.Facts

trait IfdsConstants { this: Facts =>

  /**
   * Representation of the 0 (zero) factoid
   */
  val O: Fact
}
