package ca.uwaterloo.dataflow

import java.io.File
import scala.sys.process._

object SpecUtil {

  private[this] val projectDir = System.getProperty("user.dir")

  def rebuild(dir: String, analysis: String) {
    val isWindows = System.getProperty("os.name") contains "Windows"
    if (!isWindows)
      testJavasAndJars(dir) foreach {
        case (java, jar) if !jar.exists || java.lastModified > jar.lastModified =>
          val command: Seq[String] = Seq(projectDir + "/src/test/configureSingleTest.sh", analysis, java.getName)
          Process(command, new File(projectDir + "/src/test")).!
        case _ => ()
      }
  }

  private[this] def testJavasAndJars(dir: String): Seq[(File, File)] = {
    val inputProgramDir = new File(projectDir, "src/test/scala/ca/uwaterloo/dataflow/" + dir + "/inputPrograms")
    inputProgramDir.listFiles collect {
      case file if file.isDirectory =>
        (new File(file, file.getName + ".java"), new File(file, file.getName + ".jar"))
    }
  }
}
