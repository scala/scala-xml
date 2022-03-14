import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

publish / skip := true  // root project

ThisBuild / startYear := Some(2002)
ThisBuild / licenses += (("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))

// because it doesn't declare it itself
ThisBuild / libraryDependencySchemes += "org.scala-js" %% "scalajs-library" % "semver-spec"

lazy val configSettings: Seq[Setting[_]] = Seq(
  unmanagedSourceDirectories ++= {
    unmanagedSourceDirectories.value.flatMap { dir =>
      val sv = scalaVersion.value
      Seq(
        CrossVersion.partialVersion(sv) match {
          case Some((major, minor)) if major > 2 || (major == 2 && minor >= 13)  => file(dir.getPath ++ "-2.13+")
          case _             => file(dir.getPath ++ "-2.13-")
        },
        CrossVersion.partialVersion(sv) match {
          case Some((2, _))  => file(dir.getPath ++ "-2.x")
          case _             => file(dir.getPath ++ "-3.x")
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
    name    := "scala-xml",
    scalaModuleAutomaticModuleName := Some("scala.xml"),
    crossScalaVersions := Seq("2.13.8", "2.12.15", "3.0.2", "3.1.0"),
    scalaVersion := "2.12.15",

    // Don't publish for Scala 3.1 or later, only from 3.0
    publish / skip := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, x)) if x > 0 => true
      case _                     => false
    }),

    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Seq("-language:Scala2")
      case _ =>
        // Compiler team advised avoiding the -Xsource:3 option for releases.
        // The output with -Xsource:3 should be periodically checked, though.
        Seq("-deprecation:false", "-feature", "-Xlint:-stars-align,-nullary-unit,_")
    }),

    Test / scalacOptions  += "-Xxml:coalescing",

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

    // Note: Change back to BinaryAndSourceCompatible after 2.1.0 release
    versionPolicyIntention := Compatibility.BinaryCompatible,
    // Note: See discussion on non-JVM Mima in https://github.com/scala/scala-xml/pull/517
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import com.typesafe.tools.mima.core.ProblemFilters._
      Seq(
        // afaict this is just a JDK 8 vs 16 difference, producing a false positive when
        // we compare classes built on JDK 16 (which we only do on CI, not at release time)
        // to previous-version artifacts that were built on 8.  see scala/scala-xml#501
        exclude[DirectMissingMethodProblem]("scala.xml.include.sax.XIncluder.declaration"),

        // necessitated by the switch from DefaultHandler to DefaultHandler2 in FactoryAdapter:
        exclude[MissingTypesProblem]("scala.xml.parsing.FactoryAdapter"),                         // see #549

        // necessitated by the introduction of new abstract methods in FactoryAdapter:
        exclude[ReversedMissingMethodProblem]("scala.xml.parsing.FactoryAdapter.createComment"),  // see #549
        exclude[ReversedMissingMethodProblem]("scala.xml.parsing.FactoryAdapter.createPCData")    // see #558
      )
    },
    // Mima signature checking stopped working after 3.0.2 upgrade, see #557
    mimaReportSignatureProblems := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => false
      case _ => true
    }),

    apiMappings ++= scalaInstance.value.libraryJars.filter { file =>
      file.getName.startsWith("scala-library") && file.getName.endsWith(".jar")
    }.map { libraryJar =>
      libraryJar ->
        url(s"http://www.scala-lang.org/api/${scalaVersion.value}/")
    }.toMap ++ {
      // http://stackoverflow.com/questions/16934488
      Option(System.getProperty("sun.boot.class.path")).flatMap { classPath =>
        classPath.split(java.io.File.pathSeparator).find(_.endsWith(java.io.File.separator + "rt.jar"))
      }.map { jarPath =>
        Map(
          file(jarPath)
            -> url("http://docs.oracle.com/javase/8/docs/api")
        )
      } getOrElse {
        // If everything fails, jam in Java 11 modules.
        Map(
          file("/modules/java.base")
            -> url("https://docs.oracle.com/en/java/javase/11/docs/api/java.base"),
          file("/modules/java.xml")
            -> url("https://docs.oracle.com/en/java/javase/11/docs/api/java.xml")
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
    libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.12.0" % Test,
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Seq()
      case _ =>
        Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value % Test)
    }),
  )
  .jsSettings(
    // Scala.js cannot run forked tests
    Test / fork := false
  )
  .jsEnablePlugins(ScalaJSJUnitPlugin)
  .nativeSettings(
    crossScalaVersions := Seq("2.13.8", "2.12.15", "3.1.1"),
    mimaPreviousArtifacts := {
      // TODO remove this setting when 2.0.2 released
      if (scalaBinaryVersion.value == "3") {
        mimaPreviousArtifacts.value.filterNot(_.revision == "2.0.1")
      } else {
        mimaPreviousArtifacts.value
      }
    },
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
        .getOrElse(throw new Exception("Can't find Scala Native junit-plugin jar"))
      s"-Xplugin:$jarPath"
    },
    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v"),
    // Scala Native doesn't support Scala 3.0
    Compile / nativeLink := { if(isScala30(scalaVersion.value)) null else (Compile / nativeLink).value },
    Test / nativeLink := { if(isScala30(scalaVersion.value)) null else (Test / nativeLink).value },
    Test / test := { if(isScala30(scalaVersion.value)) {} else (Test / test).value },
    Compile / sources := { if(isScala30(scalaVersion.value)) Nil else (Compile / sources).value },
    Test / sources := { if(isScala30(scalaVersion.value)) Nil else (Test / sources).value },
    libraryDependencies := { if(isScala30(scalaVersion.value)) Nil else libraryDependencies.value },
    Test / scalacOptions := { if(isScala30(scalaVersion.value)) Nil else (Test / scalacOptions).value },
    publish / skip := { isScala30(scalaVersion.value) },
  )

def isScala30(scalaVersion: String) = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((3, 0)) => true
    case _ => false
  }
}
