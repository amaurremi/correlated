package ca.uwaterloo.correlated.util

import scala.collection.JavaConverters._

object Converter {

  def toScalaIterator[T](iterator: java.util.Iterator[T]) = iterator.asScala
}
