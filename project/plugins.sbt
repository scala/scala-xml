addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.5")

val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_.nonEmpty).getOrElse("0.6.32")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1")
addSbtPlugin("org.scala-lang.modules" % "sbt-scala-module" % "2.0.0")
