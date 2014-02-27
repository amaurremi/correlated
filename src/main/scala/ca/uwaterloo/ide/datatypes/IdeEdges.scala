package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.PathEdge

trait IdeEdges { this: IdeNodes with FactInfo =>

  case class IdeEdge(source: IdeNode, target: IdeNode)

  object IdeEdge {

    def apply(pathEdge: PathEdge[Node]): IdeEdge =
      IdeEdge(
        IdeNode(pathEdge.getEntry, intToFact(pathEdge.getD1)),
        IdeNode(pathEdge.getTarget, intToFact(pathEdge.getD2)))
  }
}
