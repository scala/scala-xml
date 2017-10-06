import ScalaModulePlugin._

scalaVersionsByJvm in ThisBuild := {
  val v211 = "2.11.11"
  val v212 = "2.12.3"
  val v213 = "2.13.0-M2"
  Map(
    6 -> List(v211 -> true),
    7 -> List(v211 -> false),
    8 -> List(v212 -> true, v213 -> true, v211 -> false),
    9 -> List(v212 -> false, v213 -> false, v211 -> false))
}

lazy val root = project.in(file("."))
  .aggregate(xmlJS, xmlJVM)
  .settings(disablePublishing)

lazy val xml = crossProject.in(file("."))
  .settings(scalaModuleSettings)
  .jvmSettings(scalaModuleSettingsJVM)
  .settings(
    name    := "scala-xml",
    version := "1.1.0-SNAPSHOT",

    scalacOptions         ++= "-deprecation:false -feature -Xlint:-stars-align,-nullary-unit,_".split("\\s+").to[Seq],
    scalacOptions in Test  += "-Xxml:coalescing",

    apiMappings ++= Map(
      scalaInstance.value.libraryJar
        -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"),
      // http://stackoverflow.com/questions/16934488
      file(System.getProperty("sun.boot.class.path").split(java.io.File.pathSeparator).filter(_.endsWith(java.io.File.separator + "rt.jar")).head)
        -> url("http://docs.oracle.com/javase/8/docs/api")
    )
  )
  .jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.xml.*;version=${version.value}"),

    // there is currently no previous released JS version, therefore MiMa is enabled only on JVM
    mimaPreviousVersion := Some("1.0.6"),

    libraryDependencies += "junit" % "junit" % "4.11" % "test",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test",
    libraryDependencies += ("org.scala-lang" % "scala-compiler" % scalaVersion.value % "test").exclude("org.scala-lang.modules", s"scala-xml_${scalaVersion.value}")
  )
  .jsSettings(
    // Scala.js cannot run forked tests
    fork in Test := false
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val xmlJVM = xml.jvm
lazy val xmlJS = xml.js
