package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.types.TypeReference
import com.typesafe.config.ConfigFactory
import java.io.File
import scala.collection.JavaConverters._

trait SecretStrings extends SecretDefinition {

  private[this] val superTypeNames = Set("Ljava/lang/String", "Ljava/lang/Object")

  private[this] lazy val stringOperations: Set[String] = {
    val configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/StringOperations.conf"
    val config = ConfigFactory.parseFile(new File(System.getProperty("user.dir"), configPath))
    (config getStringList "stringOperations.ops").asScala.toSet
  }

  override def isSecret(method: IMethod) = method.getReference.getName.toString == "secret"

  override def isSecretSupertype(typeRef: TypeReference) = superTypeNames contains typeRef.getName.toString

  override def isSecretOperation(operationName: String): Boolean =
    stringOperations contains operationName
  // todo split? toCharArray? subSequence? clone?
}
