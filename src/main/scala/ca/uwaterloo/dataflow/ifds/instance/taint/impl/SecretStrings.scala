package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.types.TypeReference
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import scala.collection.JavaConverters._

trait SecretStrings extends SecretDefinition {

  private[this] case class SecretConfig(
    returnSecretString: Set[String],
    returnNonSecretString: Set[String],
    returnSecretArray: Set[String],
    returnNonSecretArray: Set[String],
    superTypes: Set[String]
  )
  
  private[this] lazy val stringConfig: SecretConfig = {
    val configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/StringOperations.conf"
    val config: Config = ConfigFactory.parseFile(new File(System.getProperty("user.dir"), configPath))
    val operationConf = config getConfig "stringOperations"
    val returnSecretString = (operationConf getStringList "secretStrings").asScala.toSet
    val returnNonSecretString = (operationConf getStringList "nonSecretStrings").asScala.toSet
    val returnSecretArray = (operationConf getStringList "secretArrays").asScala.toSet
    val returnNonSecretArray = (operationConf getStringList "nonSecretArrays").asScala.toSet
    val superTypes = (config getStringList "secretTypes.types").asScala.toSet
    SecretConfig(returnSecretString, returnNonSecretString, returnSecretArray, returnNonSecretArray, superTypes)
  }

  override def isSecret(method: IMethod) = method.getReference.getName.toString == "secret"

  override def isSecretType(typeRef: TypeReference) =
    stringConfig.superTypes contains typeRef.getName.toString

  // todo subSequence?
  override def getOperationType(op: String): Option[SecretOperation] =
    if (stringConfig.returnSecretString contains op)
      Some(ReturnsSecretString)
    else if (stringConfig.returnSecretArray contains op)
      Some(ReturnsSecretArray)
    else if (stringConfig.returnNonSecretString contains op)
      Some(ReturnsNonSecretString)
    else if (stringConfig.returnNonSecretArray contains op)
      Some(ReturnsNonSecretArray)
    else None

  override val assumeSecretByDefault = true
}
