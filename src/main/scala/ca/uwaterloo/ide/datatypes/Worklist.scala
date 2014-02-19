package ca.uwaterloo.ide

import com.ibm.wala.dataflow.IFDS.PathEdge
import com.ibm.wala.util.collections.Heap
import java.util
import scala.collection.JavaConverters._

abstract class Worklist[E] extends Heap[E](100) {

  override def compareElements(elt1: E, elt2: E) = false // todo
}

class NodeWorklist[T] extends Worklist[IdeNode[T]] {

  insert(IdeNode(???, ???)) // todo
}

class PathWorklist[T](
  private val seeds: util.Collection[PathEdge[T]]
) extends Worklist[IdeEdge[T]] {

  val edges = seeds.iterator.asScala
}