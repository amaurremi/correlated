package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.common._
import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.analysis.typeInference.TypeAbstraction
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.types.{MethodReference, TypeReference}
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import scala.collection.JavaConverters._

trait SecretStrings extends SecretDefinition {

  private[this] case class SecretConfig(
    whiteList: Set[String],
    returnSecretArray: Set[String],
    arrayElemTypes: Set[String],
    secretMethods: Set[Method],
    appendMethods: Set[AppendMethod],
    libraryOptions: LibraryOptions
  )

  private[this] case class AppendMethod(
    methodName: String,
    klass: String
  )

  private[this] def toSet[T](list: java.util.List[T]): Set[T] = list.asScala.toSet[T] 

  private[this] lazy val stringConfig: SecretConfig = {
    val configPath = "src/main/scala/ca/uwaterloo/dataflow/ifds/instance/taint/impl/StringOperations.conf"
    val config: Config = ConfigFactory.parseFile(new File(System.getProperty("user.dir"), configPath))
    val operationConf = config getConfig "stringOperations"
    val whiteList = toSet(operationConf getStringList "whiteList")
    val returnSecretArray = toSet(operationConf getStringList "secretArrays")
    val superTypes = toSet(config getStringList "secretTypes.types")
    val secretMethods = toSet(config getConfigList "secretMethods") map {
      conf =>
        Method(
          conf getString "name",
          conf getInt "params",
          conf getBoolean "static",
          conf getString "type"
        )
    }
    val appendMethods = toSet(config getConfigList "appendMethods") map {
      conf =>
        AppendMethod(
          conf getString "name",
          conf getString "class"
        )
    }
    val libConfig = config getConfig "library"
    val exclPref = toSet(libConfig getStringList "excludePrefixes")
    val defSecret = toSet(libConfig getStringList "defaultSecretTypes")
    val libWhiteList = toSet(libConfig getStringList "whiteList")
    SecretConfig(
      whiteList, 
      returnSecretArray, 
      superTypes, 
      secretMethods,
      appendMethods,
      LibraryOptions(exclPref, defSecret, libWhiteList)
    )
  }

  override def isSecret(method: IMethod) =
    stringConfig.secretMethods contains Method(method)

  override def secretTypes: Set[String] = stringConfig.secretMethods map { _.retType }

  override def isConcatClass(typeAbs: TypeAbstraction): Boolean =
    typeAbs == TypeAbstraction.TOP ||
      (stringConfig.appendMethods exists { _.klass == typeName(typeAbs.getTypeReference) })

  override def isSecretArrayElementType(typeRef: TypeReference) =
    stringConfig.arrayElemTypes contains typeName(typeRef)

  private[this] def typeName(tpe: TypeReference): String =
    tpe.getName.toString

  override def getOperationType(op: MethodReference, node: CGNode, vn: Option[ValueNumber]): Option[SecretOperation] = {
    val methodName = op.getName.toString
    val className = op.getDeclaringClass.getName.toString
    val isConcatClass = stringConfig.appendMethods exists { _.klass == className }
    val isAppendMethodName = stringConfig.appendMethods exists { _.methodName == methodName }
    val isStringClass = secretTypes contains className
    val secretArrayMethodName = stringConfig.returnSecretArray contains methodName
    val libOptions = stringConfig.libraryOptions
    val isLibCall = libOptions.excludePrefixes exists { className.startsWith }
    val retType = op.getReturnType.getName.toString
    val types = vn match {
      case Some(n) =>
        getTypes(node, n) map { _.getName.toString }
      case None    =>
        Set.empty[String]
    }
    val isDefaultSecret = (libOptions.defaultSecretTypes intersect (types + retType)).nonEmpty
    val isWhiteListedLib = isDefaultSecret && (libOptions.whiteList contains getFullName(className, methodName))

    if (secretArrayMethodName && isStringClass) // todo refactor this terrible conditional
      Some(ReturnsSecretArray)
    else if (isAppendMethodName && isConcatClass)
      Some(ConcatenatesStrings)
    else if (isConcatClass && op.isInit)
      Some (StringConcatConstructor)
    else if (methodName == "toString" && isConcatClass)
      Some(ReturnsSecretValue)
    else if (isLibCall && isDefaultSecret && !isWhiteListedLib && !isStringClass)
      Some(SecretLibraryCall)
    else if (isLibCall && (!isDefaultSecret || isDefaultSecret && isWhiteListedLib))
        Some(NonSecretLibraryCall)
    else if ((stringConfig.whiteList contains methodName) && isStringClass || !isStringClass)
      None
    else
      Some(ReturnsSecretValue)
  }

  private[this] def getFullName(className: String, methodName: String) =
    className + "/" + methodName
}
