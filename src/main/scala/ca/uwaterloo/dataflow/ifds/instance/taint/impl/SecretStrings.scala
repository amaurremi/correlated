package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.common.Method
import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.types.{MethodReference, TypeReference}
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import scala.collection.JavaConverters._

trait SecretStrings extends SecretDefinition {

  private[this] case class SecretConfig(
    whiteList: Set[String],
    returnSecretArray: Set[String],
    arrayElemTypes: Set[String]
  )
  
  private[this] lazy val stringConfig: SecretConfig = {
    val configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/StringOperations.conf"
    val config: Config = ConfigFactory.parseFile(new File(System.getProperty("user.dir"), configPath))
    val operationConf = config getConfig "stringOperations"
    val whiteList = (operationConf getStringList "whiteList").asScala.toSet
    val returnSecretArray = (operationConf getStringList "secretArrays").asScala.toSet
    val superTypes = (config getStringList "secretTypes.types").asScala.toSet
    SecretConfig(whiteList, returnSecretArray, superTypes)
  }

  override def isSecret(method: IMethod) = Method(method) == Method("secret", parameterNum = 0, isStatic = true, "String")

  override def isSecretArrayElementType(typeRef: TypeReference) =
    stringConfig.arrayElemTypes contains typeRef.getName.toString

  // todo subSequence? copyValueOf? format? getChars? valueOf?
  override def getOperationType(op: MethodReference): Option[SecretOperation] = {
    val methodName = op.getName.toString
    if (stringConfig.whiteList contains methodName)
      None
    else if (stringConfig.returnSecretArray contains methodName)
      Some(ReturnsSecretArray)
    else Some(ReturnsSecretValue)
  }
}
