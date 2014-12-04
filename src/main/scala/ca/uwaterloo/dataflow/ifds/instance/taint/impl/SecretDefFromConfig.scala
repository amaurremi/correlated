package ca.uwaterloo.dataflow.ifds.instance.taint.impl

import java.io.File

import ca.uwaterloo.dataflow.common._
import ca.uwaterloo.dataflow.ifds.instance.taint.SecretDefinition
import com.ibm.wala.analysis.typeInference.TypeAbstraction
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.types.{MethodReference, TypeReference}
import com.typesafe.config.{Config, ConfigFactory}

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
    libraryOptions: LibraryOptions,
    mainArgsSecret: Boolean
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

  override def isSecret(method: MethodReference) =
    stringConfig.secretMethods exists {
      sm =>
        sm.method equalsUpToParamNum Method(method)
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
            -1,
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
    val libWhiteList = parseConfList(libConfig, "whiteList")
    val secretIfArgument = parseConfList(libConfig, "secretIfSecretArgument")
    val mainArgsSecret = config getBoolean "mainArgsSecret"
    SecretConfig(
      whiteList, 
      returnSecretArray, 
      superTypes, 
      secretMethods,
      appendMethods,
      LibraryOptions(exclPref, defSecret, libWhiteList, secretIfArgument),
      mainArgsSecret
    )
  }

  private[this] def parseConfList(config: Config, list: String): Set[MethodNameAndClass] =
    toSet(config getConfigList list) map {
      conf =>
        MethodNameAndClass(
          conf getString "name",
          conf getString "class"
        )
    }

  override def secretTypes: Set[String] = stringConfig.secretMethods map { _.method.retType }

  override def isConcatClass(typeRef: TypeReference): Boolean =
    typeRef != null && (stringConfig.appendMethods exists {
      _.klass == typeName(typeRef)
    })

  override def isSecretArrayElementType(typeRef: TypeReference) =
    stringConfig.arrayElemTypes contains typeName(typeRef)

  lazy val typeName: TypeReference => String =
    _.getName.toString

  override def getOperationType(op: MethodReference, node: CGNode, vn: Option[ValueNumber]): Option[SecretOperation] = {
    lazy val methodName = op.getName.toString
    lazy val declaringClassName = op.getDeclaringClass.getName.toString
    lazy val isConcatClass = stringConfig.appendMethods exists {
      _.klass == declaringClassName
    }
    lazy val isAppendMethodName = stringConfig.appendMethods exists {
      _.methodName == methodName
    }
    lazy val secretArrayMethodName = stringConfig.returnSecretArray contains methodName
    lazy val libOptions = stringConfig.libraryOptions
    lazy val isLibCall = libOptions.excludePrefixes exists {
      declaringClassName.startsWith
    }
    lazy val retType = op.getReturnType.getName.toString
    lazy val hasSecretReturnType = secretTypes contains retType
    lazy val isInvokedOnSecretType = secretTypes contains declaringClassName
    lazy val types = vn match {
      case Some(n) =>
        getTypes(node, n) map {
          _.getName.toString
        }
      case None =>
        Set.empty[String]
    }
    // Is the return type (or its subtype) considered secret by default, if it's returned from a library?
    lazy val isDefaultSecret = (libOptions.defaultSecretTypes intersect (types + retType)).nonEmpty
    lazy val isWhiteListedLib = isDefaultSecret && (libOptions.whiteList exists {
      m =>
        m.methodName == methodName && isSubType(op.getDeclaringClass, m.klass, node.getClassHierarchy)
    })
    lazy val isInSecretIfSecretArgList = isDefaultSecret && (libOptions.secretIfArgument exists {
      m =>
        m.methodName == methodName && isSubType(op.getDeclaringClass, m.klass, node.getClassHierarchy)
    })
    lazy val stringTypeConsideredSecret = secretTypes contains "Ljava/lang/String"

    if (secretArrayMethodName && hasSecretReturnType) // todo refactor this terrible conditional
      Some(ReturnsSecretArray)
    else if (isAppendMethodName && isConcatClass)
      Some(ConcatenatesStrings)
    else if (isConcatClass && op.isInit)
      Some(StringConcatConstructor)
    else if (methodName == "toString" && isConcatClass && stringTypeConsideredSecret)
      Some(ReturnsStaticSecretOrPreservesSecret)
    else if (isLibCall && isInSecretIfSecretArgList)
      Some(SecretIfSecretArgument)
    else if (isLibCall && isDefaultSecret && !isWhiteListedLib && !isInvokedOnSecretType)
      Some(SecretLibraryCall)
    else if (isLibCall && (!isDefaultSecret || isDefaultSecret && isWhiteListedLib))
      Some(NonSecretLibraryCall)
    else if ((stringConfig.whiteList contains methodName) && isInvokedOnSecretType || !isInvokedOnSecretType)
      None
    else
      Some(ReturnsStaticSecretOrPreservesSecret)
  }

  override def mainArgsSecret: Boolean =
    stringConfig.mainArgsSecret
}
