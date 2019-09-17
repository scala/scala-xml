val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("0.6.28")

addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module" % "2.1.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.3.4")
