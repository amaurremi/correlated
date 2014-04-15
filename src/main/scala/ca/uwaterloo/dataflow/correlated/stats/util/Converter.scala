package ca.uwaterloo.dataflow.correlated.stats.util

import scala.collection.JavaConverters._

object Converter {

  def toScalaIterator[T](iterator: java.util.Iterator[T]) = iterator.asScala

  def toScalaList[T](iterator: java.util.Iterator[T]) = toScalaIterator(iterator).toList

  def toScalaSet[T](set: java.util.Set[T]) = set.asScala.toSet
}
