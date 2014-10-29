name := "Correlated Calls"

version := "0.1"

organization := "University of Waterloo"

scalaVersion := "2.10.0"

mainClass in (Compile, run) := Some("ca.uwaterloo.dataflow.correlated.collector.CcBenchmarkRunner")

resolvers += "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.+",
  "com.typesafe" % "config" % "0.5.+",
  "com.ibm.wala" % "com.ibm.wala.shrike" % "1.3.4-SNAPSHOT",
  "com.ibm.wala" % "com.ibm.wala.util" % "1.3.4-SNAPSHOT",
  "com.ibm.wala" % "com.ibm.wala.core" % "1.3.4-SNAPSHOT",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "org.scalamock" %% "scalamock-specs2-support" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "1.9.5",
  "org.scalaz" %% "scalaz-core" % "7.0.5")

org.scalastyle.sbt.ScalastylePlugin.Settings