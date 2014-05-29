package ca.uwaterloo.dataflow.common

import com.ibm.wala.classLoader.IMethod

case class Method(
  name: String,
  parameterNum: Int,
  isStatic: Boolean,
  retType: String
)

object Method {

  def apply(method: IMethod): Method =
    Method(
      method.getName.toString,
      if (method.isStatic) method.getNumberOfParameters else method.getNumberOfParameters - 1,
      method.isStatic,
      method.getReturnType.getName.toString
    )
}