package ca.uwaterloo.ide

import java.util
import scala.collection.mutable
import com.ibm.wala.dataflow.IFDS.PathEdge
import scala.collection.JavaConverters._

object Util {

  def mutableMap[K, W](t: TraversableOnce[(K, W)]) =
    mutable.Map(t.toSeq: _*)

  def getSeeds[T] = (seeds: util.Collection[PathEdge[T]]) => seeds.iterator.asScala

  def seedNodes[T] =
    (seeds: util.Collection[PathEdge[T]]) =>
      getSeeds(seeds) map {
        seed =>
          IdeNode(seed.getEntry, Fact(seed.getD1)) // todo correct?
      }
}
