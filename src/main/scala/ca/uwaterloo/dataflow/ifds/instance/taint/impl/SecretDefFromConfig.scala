package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import ca.uwaterloo.dataflow.common._
import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.analysis.typeInference.TypeAbstraction
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.types.{MethodReference, TypeReference}
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import scala.collection.JavaConverters._

trait SecretDefFromConfig extends SecretDefinition {

  def configPath: String

  protected case class SecretMethod(method: Method, enclClass: String)

  protected case class SecretConfig(
    whiteList: Set[String],
    returnSecretArray: Set[String],
    arrayElemTypes: Set[String],
    secretMethods: Set[SecretMethod],
    appendMethods: Set[MethodNameAndClass],
    libraryOptions: LibraryOptions
  )

  private[this] def toSet[T](list: java.util.List[T]): Set[T] = list.asScala.toSet[T]

  private[this] def isSubType(c: TypeReference, of: String, cha: IClassHierarchy): Boolean = {
    val name = typeName(c)
    lazy val amongSubtypes = cha.computeSubClasses(c).asScala exists {
      s =>
        typeName(s.getReference) == name
    }
    name == of || amongSubtypes
  }

  override def isSecret(method: IMethod) =
    stringConfig.secretMethods exists {
      sm =>
        sm.method == Method(method) && isSubType(method.getDeclaringClass.getReference, sm.enclClass, method.getClassHierarchy)
    }

  lazy val stringConfig: SecretConfig = {
    val config: Config = ConfigFactory.parseFile(new File(System.getProperty("user.dir"), configPath))
    val operationConf = config getConfig "stringOperations"
    val whiteList = toSet(operationConf getStringList "whiteList")
    val returnSecretArray = toSet(operationConf getStringList "secretArrays")
    val superTypes = toSet(config getStringList "secretTypes.types")
    val secretMethods = toSet(config getConfigList "secretMethods") map {
      conf =>
        SecretMethod(
          Method(
            conf getString "name",
            conf getInt "params",
            conf getBoolean "static",
            conf getString "type"
          ),
          conf getString "enclosing"
        )
    }
    val appendMethods = toSet(config getConfigList "appendMethods") map {
      conf =>
        MethodNameAndClass(
          conf getString "name",
          conf getString "class"
        )
    }
    val libConfig = config getConfig "library"
    val exclPref = toSet(libConfig getStringList "excludePrefixes")
    val defSecret = toSet(libConfig getStringList "defaultSecretTypes")
    val libWhiteList = toSet(libConfig getConfigList "whiteList") map {
      conf =>
        MethodNameAndClass(
          conf getString "name",
          conf getString "class"
        )
    }
    SecretConfig(
      whiteList, 
      returnSecretArray, 
      superTypes, 
      secretMethods,
      appendMethods,
      LibraryOptions(exclPref, defSecret, libWhiteList)
    )
  }

  override def secretTypes: Set[String] = stringConfig.secretMethods map { _.method.retType }

  override def isConcatClass(typeAbs: TypeAbstraction): Boolean =
    typeAbs != TypeAbstraction.TOP &&
      (stringConfig.appendMethods exists {
        _.klass == typeName(typeAbs.getTypeReference)
      })

  override def isSecretArrayElementType(typeRef: TypeReference) =
    stringConfig.arrayElemTypes contains typeName(typeRef)

  def typeName(tpe: TypeReference): String =
    tpe.getName.toString

  override def getOperationType(op: MethodReference, node: CGNode, vn: Option[ValueNumber]): Option[SecretOperation] = {
    val methodName = op.getName.toString
    val declaringClassName = op.getDeclaringClass.getName.toString
    val isConcatClass = stringConfig.appendMethods exists { _.klass == declaringClassName }
    val isAppendMethodName = stringConfig.appendMethods exists { _.methodName == methodName }
    val secretArrayMethodName = stringConfig.returnSecretArray contains methodName
    val libOptions = stringConfig.libraryOptions
    val isLibCall = libOptions.excludePrefixes exists { declaringClassName.startsWith }
    val retType = op.getReturnType.getName.toString
    val isSecretType = secretTypes contains retType
    val types = vn match {
      case Some(n) =>
        getTypes(node, n) map { _.getName.toString }
      case None    =>
        Set.empty[String]
    }
    val isDefaultSecret = (libOptions.defaultSecretTypes intersect (types + retType)).nonEmpty
    val isWhiteListedLib = isDefaultSecret && (libOptions.whiteList exists {
      m =>
        m.methodName == methodName && isSubType(op.getDeclaringClass, m.klass, node.getClassHierarchy)
    })
    val stringTypeConsideredSecret = secretTypes contains "Ljava/lang/String"

    if (secretArrayMethodName && isSecretType) // todo refactor this terrible conditional
      Some(ReturnsSecretArray)
    else if (isAppendMethodName && isConcatClass)
      Some(ConcatenatesStrings)
    else if (isConcatClass && op.isInit)
      Some(StringConcatConstructor)
    else if (methodName == "toString" && isConcatClass && stringTypeConsideredSecret)
      Some(PreservesSecretValue)
    else if (isLibCall && isDefaultSecret && !isWhiteListedLib && !isSecretType)
      Some(SecretLibraryCall)
    else if (isLibCall && (!isDefaultSecret || isDefaultSecret && isWhiteListedLib))
      Some(NonSecretLibraryCall)
    else if ((stringConfig.whiteList contains methodName) && isSecretType || !isSecretType)
      None
    else
      Some(PreservesSecretValue)
  }
}
