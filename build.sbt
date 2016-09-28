import com.typesafe.tools.mima.plugin.{MimaPlugin, MimaKeys}

scalaVersion       in ThisBuild := crossScalaVersions.value.head
crossScalaVersions in ThisBuild := {
  val java = System.getProperty("java.version")
  if (java.startsWith("1.6.") || java.startsWith("1.7."))
    Seq("2.11.8")
  else if (java.startsWith("1.8.") || java.startsWith("1.9."))
    Seq("2.12.0-RC1")
  else
    sys.error(s"don't know what Scala versions to build on $java")
}

lazy val root = project.in(file("."))
  .aggregate(xmlJS, xmlJVM)
  .settings(publish := {}, publishLocal := {})

lazy val xml = crossProject.in(file("."))
  .settings(
    name    := "scala-xml",
    version := "1.0.6-SNAPSHOT",
    scalacOptions         ++= "-deprecation:false -feature -Xlint:-stars-align,-nullary-unit,_".split("\\s+").to[Seq],
    scalacOptions in Test  += "-Xxml:coalescing")
  .jvmSettings(
    scalaModuleSettings ++
    scalaModuleOsgiSettings ++
    List(
      OsgiKeys.exportPackage := Seq(s"scala.xml.*;version=${version.value}"),
      libraryDependencies += "junit" % "junit" % "4.11" % "test",
      libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test",
      libraryDependencies += ("org.scala-lang" % "scala-compiler" % scalaVersion.value % "test").exclude("org.scala-lang.modules", s"scala-xml*"),
      mimaPreviousVersion := Some("1.0.5"),
      // You cannot disable JVM test forking when working on scala modules
      // that are distributed with the compiler because of an SBT
      // classloader leaking issue (scala/scala-xml#20 and #112).
      fork in Test := true): _*)
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val xmlJVM = xml.jvm
lazy val xmlJS = xml.js
