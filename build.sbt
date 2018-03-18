import ScalaModulePlugin._

scalaVersionsByJvm in ThisBuild := {
  val v211 = "2.11.12"
  val v212 = "2.12.4"
  val v213 = "2.13.0-M3"
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
    version := "1.1.1-SNAPSHOT",

    scalacOptions         ++= "-deprecation:false -feature -Xlint:-stars-align,-nullary-unit,_".split("\\s+").to[Seq],
    scalacOptions in Test  += "-Xxml:coalescing",

    apiMappings ++= Map(
      scalaInstance.value.libraryJar
        -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")
    ) ++ {
      // http://stackoverflow.com/questions/16934488
      Option(System.getProperty("sun.boot.class.path")).flatMap { classPath =>
        classPath.split(java.io.File.pathSeparator).filter(_.endsWith(java.io.File.separator + "rt.jar")).headOption
      }.map { jarPath =>
        Map(
          file(jarPath)
            -> url("http://docs.oracle.com/javase/8/docs/api")
        )
      } getOrElse {
        // If everything fails, jam in the Java 9 base module.
        Map(
          file("/modules/java.base")
            -> url("http://docs.oracle.com/javase/9/docs/api"),
          file("/modules/java.xml")
            -> url("http://docs.oracle.com/javase/9/docs/api")

        )
      }
    }
  )
  .jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.xml.*;version=${version.value}"),

    // there is currently no previous released JS version, therefore MiMa is enabled only on JVM
    mimaPreviousVersion := Some("1.0.6"),

    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import com.typesafe.tools.mima.core.ProblemFilters._
      Seq(
        // scala-xml 1.1.1 deprecated XMLEventReader, so we broke
        // binary compatibility for 1.1.1 in the following way:
        exclude[MissingClassProblem]("scala.xml.pull.EvComment"),
        exclude[MissingClassProblem]("scala.xml.pull.EvComment$"),
        exclude[MissingClassProblem]("scala.xml.pull.EvElemEnd"),
        exclude[MissingClassProblem]("scala.xml.pull.EvElemEnd$"),
        exclude[MissingClassProblem]("scala.xml.pull.EvElemStart"),
        exclude[MissingClassProblem]("scala.xml.pull.EvElemStart$"),
        exclude[MissingClassProblem]("scala.xml.pull.EvEntityRef"),
        exclude[MissingClassProblem]("scala.xml.pull.EvEntityRef$"),
        exclude[MissingClassProblem]("scala.xml.pull.EvProcInstr"),
        exclude[MissingClassProblem]("scala.xml.pull.EvProcInstr$"),
        exclude[MissingClassProblem]("scala.xml.pull.EvText"),
        exclude[MissingClassProblem]("scala.xml.pull.EvText$"),
        exclude[MissingClassProblem]("scala.xml.pull.ExceptionEvent"),
        exclude[MissingClassProblem]("scala.xml.pull.ExceptionEvent$"),
        exclude[MissingClassProblem]("scala.xml.pull.ProducerConsumerIterator"),
        exclude[MissingClassProblem]("scala.xml.pull.XMLEvent"),
        exclude[MissingClassProblem]("scala.xml.pull.XMLEventReader"),
        exclude[MissingClassProblem]("scala.xml.pull.XMLEventReader$POISON$"),
        exclude[MissingClassProblem]("scala.xml.pull.XMLEventReader$Parser"),
        exclude[MissingClassProblem]("scala.xml.pull.package"),
        exclude[MissingClassProblem]("scala.xml.pull.package$"),
        exclude[MissingTypesProblem]("scala.xml.Atom"),
        exclude[MissingTypesProblem]("scala.xml.Comment"),
        exclude[MissingTypesProblem]("scala.xml.Document"),
        exclude[MissingTypesProblem]("scala.xml.EntityRef"),
        exclude[MissingTypesProblem]("scala.xml.PCData"),
        exclude[MissingTypesProblem]("scala.xml.ProcInstr"),
        exclude[MissingTypesProblem]("scala.xml.SpecialNode"),
        exclude[MissingTypesProblem]("scala.xml.Text"),
        exclude[MissingTypesProblem]("scala.xml.Unparsed")
      )
    },

    libraryDependencies += "junit" % "junit" % "4.12" % "test",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.5" % "test",
    libraryDependencies += ("org.scala-lang" % "scala-compiler" % scalaVersion.value % "test").exclude("org.scala-lang.modules", s"scala-xml_${scalaBinaryVersion.value}")
  )
  .jsSettings(
    // Scala.js cannot run forked tests
    fork in Test := false
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val xmlJVM = xml.jvm
lazy val xmlJS = xml.js
