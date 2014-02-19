package ca.uwaterloo.ide

import scala.collection.mutable

object Util {

  def mutableMap[K, W](t: TraversableOnce[(K, W)]) =
    mutable.Map(t.toSeq: _*)
}
