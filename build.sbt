import ScalaModulePlugin._

scalaVersionsByJvm in ThisBuild := {
  val v211 = "2.11.11"
  val v212 = "2.12.2"
  val v213 = "2.13.0-M1"
  Map(
    6 -> List(v211 -> true),
    7 -> List(v211 -> false),
    8 -> List(v212 -> true, v213 -> true, v211 -> false),
    9 -> List(v212 -> false, v213 -> false, v211 -> false))
}

lazy val root = project.in(file("."))
  .aggregate(xmlJS, xmlJVM)
  .settings(publish := {}, publishLocal := {})

lazy val xml = crossProject.in(file("."))
  .settings(
    name    := "scala-xml",
    version := "1.0.7-SNAPSHOT",
    scalacOptions         ++= "-deprecation:false -feature -Xlint:-stars-align,-nullary-unit,_".split("\\s+").to[Seq],
    scalacOptions in Test  += "-Xxml:coalescing",
    apiMappings += (
      scalaInstance.value.libraryJar
        -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")
    )
  )
  .jvmSettings(
    scalaModuleSettings ++
    List(
      OsgiKeys.exportPackage := Seq(s"scala.xml.*;version=${version.value}"),
      libraryDependencies += "junit" % "junit" % "4.11" % "test",
      libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test",
      libraryDependencies += ("org.scala-lang" % "scala-compiler" % scalaVersion.value % "test").exclude("org.scala-lang.modules", s"scala-xml*"),
      mimaPreviousVersion := Some("1.0.6")): _*)
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val xmlJVM = xml.jvm
lazy val xmlJS = xml.js
