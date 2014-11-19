package ca.uwaterloo.dataflow.correlated.collector

import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.types.ClassLoaderReference

object AppCorrelatedCallStats {

  def apply(cg: CallGraph): CorrelatedCallStats = {
    val allStats = CorrelatedCallStats.apply(cg)
    val isAppMethod: IMethod => Boolean = {
      _.getDeclaringClass.getClassLoader.getReference == ClassLoaderReference.Application
    }
    allStats.copy(
      cgNodes = allStats.cgNodes filter { node =>
        isAppMethod(node.getMethod)
      },
      rcs = allStats.rcs map {
        nodeSet =>
          nodeSet filter {
            node =>
              isAppMethod(node.getMethod)
          }
      } filter { _.nonEmpty },
      rcCcReceivers = allStats.rcCcReceivers filter {
        case Receiver(_, method) =>
          isAppMethod(method)
      },
      receiverToCallSites = allStats.receiverToCallSites filter {
        case (Receiver(_, method), _) =>
          isAppMethod(method)
      },
      totalCallSites = allStats.totalCallSites filter {
        case (_, cgNode) =>
          isAppMethod(cgNode.getMethod)
      },
      polymorphicCallSites = allStats.polymorphicCallSites filter {
        case (_, cgNode) =>
          isAppMethod(cgNode.getMethod)
      }
    )
  }
}
