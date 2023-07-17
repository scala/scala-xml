import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

publish / skip := true  // root project

ThisBuild / startYear := Some(2002)
ThisBuild / licenses += (("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))

// because it doesn't declare it itself
ThisBuild / libraryDependencySchemes += "org.scala-js" %% "scalajs-library" % "semver-spec"
ThisBuild / apiURL := Some(url("https://javadoc.io/doc/org.scala-lang.modules/scala-xml_2.13/"))

lazy val configSettings: Seq[Setting[?]] = Seq(
  unmanagedSourceDirectories ++= {
    unmanagedSourceDirectories.value.flatMap { dir =>
      def forVersion(version: String): File = file(dir.getPath ++ "-" ++ version)
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) => Seq(forVersion("2.13"))
        case _ => Seq(forVersion("2.11-2.12"))
      }
    }
  }
)

lazy val xml = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(ScalaModulePlugin.scalaModuleSettings)
  .jvmSettings(ScalaModulePlugin.scalaModuleOsgiSettings)
  .settings(
    name    := "scala-xml",
    scalaModuleAutomaticModuleName := Some("scala.xml"),
    crossScalaVersions := Seq("2.13.11", "2.12.18", "2.11.12"),
    scalaVersion := "2.12.18",

    scalacOptions ++= Seq("-deprecation:false", "-feature", "-Xlint:-stars-align,-nullary-unit,_"),

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

    versionPolicyIntention := Compatibility.BinaryAndSourceCompatible,

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
      }.getOrElse {
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
    libraryDependencies += ("org.scala-lang" % "scala-compiler" % scalaVersion.value % Test).exclude("org.scala-lang.modules", s"scala-xml_${scalaBinaryVersion.value}")
  )
  .jsSettings(
    crossScalaVersions := crossScalaVersions.value.filterNot(_.startsWith("2.11")),
    // Scala.js cannot run forked tests
    Test / fork := false
  )
  .jsEnablePlugins(ScalaJSJUnitPlugin)
