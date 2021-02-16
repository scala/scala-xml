import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / startYear := Some(2002)
ThisBuild / licenses += (("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))

lazy val configSettings: Seq[Setting[_]] = Seq(
  unmanagedSourceDirectories ++= {
    unmanagedSourceDirectories.value.flatMap { dir =>
      val sv = scalaVersion.value
      Seq(
        CrossVersion.partialVersion(sv) match {
          case Some((major, minor))
              if major > 2 || (major == 2 && minor >= 13) =>
            file(dir.getPath ++ "-2.13+")
          case _ => file(dir.getPath ++ "-2.13-")
        },
        CrossVersion.partialVersion(sv) match {
          case Some((2, _)) => file(dir.getPath ++ "-2.x")
          case _            => file(dir.getPath ++ "-3.x")
        }
      )
    }
  }
)

lazy val xml = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(ScalaModulePlugin.scalaModuleSettings)
  .jvmSettings(ScalaModulePlugin.scalaModuleOsgiSettings)
  .settings(
    name := "scala-xml",
    scalacOptions ++= {
      val opts =
        if (isDotty.value)
          "-language:Scala2"
        else
          // Compiler team advised avoiding the -Xsource:2.14 option for releases.
          // The output with -Xsource should be periodically checked, though.
          "-deprecation:false -feature -Xlint:-stars-align,-nullary-unit,_"
      opts.split("\\s+").to[Seq]
    },
    Test / scalacOptions += "-Xxml:coalescing",
    // don't run Dottydoc, it errors and isn't needed anyway.
    // but we leave `publishArtifact` set to true, otherwise Sonatype won't let us publish
    Compile / doc / sources := (if (isDotty.value) Seq() else (Compile / doc/ sources).value),

    headerLicense  := Some(HeaderLicense.Custom(
      s"""|Scala (https://www.scala-lang.org)
          |
          |Copyright EPFL and Lightbend, Inc.
          |
          |Licensed under Apache License 2.0
          |(http://www.apache.org/licenses/LICENSE-2.0).
          |
          |See the NOTICE file distributed with this work for
          |additional information regarding copyright ownership.
          |""".stripMargin)),

    scalaModuleMimaPreviousVersion := {
      if (isDotty.value) None // No such release yet
      else Some("1.3.0")
    },
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import com.typesafe.tools.mima.core.ProblemFilters._
      Seq(
        // scala-xml 1.1.1 deprecated XMLEventReader, so it broke
        // binary compatibility for 2.0.0 in the following way:
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
        exclude[MissingTypesProblem]("scala.xml.Unparsed"),
        // Miscellaneous deprecations
        exclude[MissingClassProblem]("scala.xml.dtd.impl.PointedHedgeExp"),
        exclude[MissingClassProblem](
          "scala.xml.dtd.impl.PointedHedgeExp$TopIter$"
        ),
        exclude[MissingClassProblem](
          "scala.xml.dtd.impl.PointedHedgeExp$Node$"
        ),
        exclude[MissingClassProblem](
          "scala.xml.dtd.impl.PointedHedgeExp$Point$"
        ),
        exclude[MissingClassProblem](
          "scala.xml.dtd.impl.PointedHedgeExp$TopIter"
        ),
        exclude[MissingClassProblem]("scala.xml.dtd.impl.PointedHedgeExp$Node"),
        exclude[MissingClassProblem]("scala.xml.dtd.Scanner"),
        exclude[MissingClassProblem]("scala.xml.dtd.ContentModelParser$"),
        exclude[MissingClassProblem]("scala.xml.dtd.ContentModelParser"),
        exclude[MissingClassProblem]("scala.xml.dtd.ElementValidator"),
        exclude[MissingClassProblem]("scala.xml.dtd.ElementValidator"),
        exclude[MissingClassProblem]("scala.xml.factory.Binder"),
        exclude[MissingClassProblem](
          "scala.xml.parsing.ValidatingMarkupHandler"
        ),
        exclude[MissingClassProblem]("scala.xml.persistent.CachedFileStorage"),
        exclude[MissingClassProblem]("scala.xml.persistent.Index"),
        exclude[MissingClassProblem]("scala.xml.persistent.SetStorage"),
        exclude[DirectMissingMethodProblem]("scala.xml.dtd.ContentModel.parse"),
        exclude[DirectMissingMethodProblem]("scala.xml.Elem.this"),
        exclude[DirectMissingMethodProblem]("scala.xml.Elem.apply"),
        exclude[DirectMissingMethodProblem]("scala.xml.Elem.processXml"),
        exclude[DirectMissingMethodProblem]("scala.xml.Elem.xmlToProcess"),
        // Scala 2.12 deprecated mutable.Stack, so we broke
        // binary compatibility for 2.0.0 in the following way:
        exclude[IncompatibleMethTypeProblem](
          "scala.xml.parsing.FactoryAdapter.scopeStack_="
        ),
        exclude[IncompatibleResultTypeProblem](
          "scala.xml.parsing.FactoryAdapter.hStack"
        ),
        exclude[IncompatibleResultTypeProblem](
          "scala.xml.parsing.FactoryAdapter.scopeStack"
        ),
        exclude[IncompatibleResultTypeProblem](
          "scala.xml.parsing.FactoryAdapter.attribStack"
        ),
        exclude[IncompatibleResultTypeProblem](
          "scala.xml.parsing.FactoryAdapter.tagStack"
        ),
        // New MiMa checks for generic signature changes
        exclude[IncompatibleSignatureProblem]("*")
      )
    },
    apiMappings ++= scalaInstance.value.libraryJars
      .filter { file =>
        file.getName.startsWith("scala-library") && file.getName.endsWith(
          ".jar"
        )
      }
      .map { libraryJar =>
        libraryJar ->
          url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")
      }
      .toMap ++ {
      // http://stackoverflow.com/questions/16934488
      Option(System.getProperty("sun.boot.class.path"))
        .flatMap { classPath =>
          classPath
            .split(java.io.File.pathSeparator)
            .find(_.endsWith(java.io.File.separator + "rt.jar"))
        }
        .map { jarPath =>
          Map(
            file(jarPath)
              -> url("http://docs.oracle.com/javase/8/docs/api")
          )
        } getOrElse {
        // If everything fails, jam in Java 11 modules.
        Map(
          file("/modules/java.base")
            -> url(
              "https://docs.oracle.com/en/java/javase/11/docs/api/java.base"
            ),
          file("/modules/java.xml")
            -> url(
              "https://docs.oracle.com/en/java/javase/11/docs/api/java.xml"
            )
        )
      }
    }
  )
  .settings(
    inConfig(Compile)(configSettings) ++ inConfig(Test)(configSettings)
  )
  .jvmSettings(
    OsgiKeys.exportPackage := Seq(s"scala.xml.*;version=${version.value}"),

    libraryDependencies += "junit" % "junit" % "4.13.2" % Test,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.11" % Test,
    libraryDependencies ++= {
      if (isDotty.value)
        Seq()
      else
        Seq(
          ("org.scala-lang" % "scala-compiler" % scalaVersion.value % Test)
            .exclude(
              "org.scala-lang.modules",
              s"scala-xml_${scalaBinaryVersion.value}"
            )
        )
    }
  )
  .jsSettings(
    // Scala.js cannot run forked tests
    Test / fork := false
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .nativeSettings(
    scalaModuleMimaPreviousVersion := None, // No such release yet
    // Scala Native cannot run forked tests
    Test / fork := false,
    libraryDependencies += "org.scala-native" %%% "junit-runtime" % nativeVersion % Test,
    Test / scalacOptions += {
      val log = streams.value.log
      val retrieveDir = baseDirectory.value / "scala-native-junit-plugin-jars"
      val lm = dependencyResolution.value
      val cp = lm
        .retrieve(
          "org.scala-native" % s"junit-plugin_${scalaVersion.value}" % nativeVersion,
          scalaModuleInfo = None,
          retrieveDir,
          log
        )
        .fold(w => throw w.resolveException, identity(_))
      val jarPath = cp
        .find(_.toString.contains("junit-plugin"))
        .getOrElse(
          throw new Exception("Can't find Scala Native junit-plugin jar")
        )
      s"-Xplugin:$jarPath"
    },
    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v")
  )
