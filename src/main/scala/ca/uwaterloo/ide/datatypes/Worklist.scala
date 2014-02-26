package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.{ISupergraph, PathEdge}
import com.ibm.wala.util.collections.Heap
import java.util
import scala.collection.JavaConverters._

object Worklist {
  val capacity: Int = 100
}

abstract class Worklist[E](
  initialElements: Seq[E],
  capacity: Int = Worklist.capacity
) extends Heap[E](capacity) {

  initialElements map insert // todo I'm not sure about whether it's correct to use Heap like that

  override def compareElements(elt1: E, elt2: E) = false // todo
}

object NodeWorklist {
  def createSeedNodes[T, P](seeds: java.util.Collection[T], zero: Fact)(implicit supergraph: ISupergraph[T, P]) =
    seeds.asScala.toSeq map { IdeNode(_, zero) }
}

import NodeWorklist.createSeedNodes

class NodeWorklist[T, P](
  seeds: java.util.Collection[T],
  zero: Fact
)(
  implicit supergraph: ISupergraph[T, P]
) extends Worklist[IdeNode[T]](createSeedNodes(seeds, zero))

object PathWorklist {
  def createSeedEdges[T, P](
    pathEdges: util.Collection[PathEdge[T]]
  )(
    implicit supergraph: ISupergraph[T, P]
  ): Seq[IdeEdge[T]] =
    pathEdges.asScala.toSeq map {
      IdeEdge.apply(_)
    }
}

import PathWorklist.createSeedEdges

class PathWorklist[T, P](
  private val seeds: util.Collection[PathEdge[T]]
)(
  implicit supergraph: ISupergraph[T, P]
) extends Worklist[IdeEdge[T]](createSeedEdges(seeds))
