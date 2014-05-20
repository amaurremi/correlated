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
    arrayElemTypes: Set[String],
    secretMethod: Method,
    appendMethod: AppendMethod
  )

  private[this] case class AppendMethod(
    methodName: String,
    classes: Set[String]
  )

  private[this] def toSet[T](list: java.util.List[T]): Set[T] = list.asScala.toSet[T] 

  private[this] lazy val stringConfig: SecretConfig = {
    val configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/StringOperations.conf"
    val config: Config = ConfigFactory.parseFile(new File(System.getProperty("user.dir"), configPath))
    val operationConf = config getConfig "stringOperations"
    val whiteList = toSet(operationConf getStringList "whiteList")
    val returnSecretArray = toSet(operationConf getStringList "secretArrays")
    val superTypes = toSet(config getStringList "secretTypes.types")
    val methodConfig = config getConfig "secretMethod"
    val name = methodConfig getString "name"
    val tpe = methodConfig getString "type"
    val params = methodConfig getInt "params"
    val static = methodConfig getBoolean "static"
    val appendConfig = config getConfig "appendMethod"
    val appendMethodName = appendConfig getString "name"
    val appendClasses = toSet(appendConfig getStringList "classes")
    SecretConfig(
      whiteList, 
      returnSecretArray, 
      superTypes, 
      Method(name, params, static, tpe), 
      AppendMethod(appendMethodName, appendClasses)
    )
  }

  override def isSecret(method: IMethod) =
    Method(method) == stringConfig.secretMethod

  override def secretType: String = stringConfig.secretMethod.retType

  override def isSecretArrayElementType(typeRef: TypeReference) =
    stringConfig.arrayElemTypes contains typeRef.getName.toString

  // todo subSequence? copyValueOf? format? getChars? valueOf?
  override def getOperationType(op: MethodReference): Option[SecretOperation] = {
    val methodName = op.getName.toString
    if (stringConfig.whiteList contains methodName)
      None
    else if (stringConfig.returnSecretArray contains methodName)
      Some(ReturnsSecretArray)
    else if (stringConfig.appendMethod.methodName == methodName && (stringConfig.appendMethod.classes contains op.getDeclaringClass.getName.toString))
      Some(ConcatenatesStrings)
    else
      Some(ReturnsSecretValue)
  }
}
