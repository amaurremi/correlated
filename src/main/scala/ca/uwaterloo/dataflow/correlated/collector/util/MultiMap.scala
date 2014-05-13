package ca.uwaterloo.dataflow.correlated.collector.util

import ca.uwaterloo.dataflow.correlated.collector.MultiMap

object MultiMap {

  def empty[A, B]: MultiMap[A, B] = Map.empty

  def mergeMultiMaps[A, B](
    map1: MultiMap[A, B],
    map2: MultiMap[A, B]
  ): MultiMap[A, B] = {
    val keyIntersection = map1.keySet intersect map2.keySet
    val intersectMap: MultiMap[A, B] = (keyIntersection map {
      key =>
        key -> (map1(key) ++ map2(key))
    }).toMap
    map1 ++ map2 ++ intersectMap
  }

  def mergeMultiMapList[A, B](
    multiMaps: List[MultiMap[A, B]]
  ): MultiMap[A, B] =
    multiMaps.foldLeft(empty[A, B])(mergeMultiMaps)
}
