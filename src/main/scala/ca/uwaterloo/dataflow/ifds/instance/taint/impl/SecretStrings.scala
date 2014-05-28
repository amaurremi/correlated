package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import com.ibm.wala.classLoader.IMethod
import scala.collection.JavaConverters._

trait SecretStrings extends SecretDefFromConfig {

  override def configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/resources/SecretStrings.conf"
}
