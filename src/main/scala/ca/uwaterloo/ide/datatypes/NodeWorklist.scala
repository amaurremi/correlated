package ca.uwaterloo.ide

import com.ibm.wala.util.collections.Heap

class NodeWorklist[T] extends Heap[IdeNode[T]](100) {

  insert(IdeNode(???, ???)) // todo

  override def compareElements(elt1: IdeNode[T], elt2: IdeNode[T]) = false // todo
}
