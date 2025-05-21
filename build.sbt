import com.typesafe.tools.mima.core._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

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
        case Some((3, _)) => Seq(forVersion("3"), forVersion("2.13+"))
        case Some((2, minor)) =>
          Seq(forVersion("2")) ++ (minor match {
            case 13 => Seq(forVersion("2.13"), forVersion("2.13+"))
            case 12 => Seq(forVersion("2.12"))
            case _ => Seq()
          })
        case _ => Seq()
      }
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
    crossScalaVersions := Seq("2.13.16", "2.12.20", "3.3.6"),
    scalaVersion := "2.12.20",

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

    versionPolicyIntention := Compatibility.BinaryCompatible,
    // Note: See discussion on non-JVM Mima in https://github.com/scala/scala-xml/pull/517
    mimaBinaryIssueFilters ++= {
      //import com.typesafe.tools.mima.core.{}
      //import com.typesafe.tools.mima.core.ProblemFilters
      Seq( // exclusions for all Scala versions
        // new method in `Node` with return type `immutable.Seq`
        ProblemFilters.exclude[ReversedMissingMethodProblem]("scala.xml.Node.child"),

        // these used to be declared methods, but are now bridges without generic signature
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Node.nonEmptyChildren"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Group.child"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.SpecialNode.child"),

        // new methods with return type immutable.Seq
        ProblemFilters.exclude[ReversedMissingMethodProblem]("scala.xml.Attribute.apply"),
        ProblemFilters.exclude[ReversedMissingMethodProblem]("scala.xml.Attribute.value"),
        ProblemFilters.exclude[ReversedMissingMethodProblem]("scala.xml.MetaData.apply"),
        ProblemFilters.exclude[ReversedMissingMethodProblem]("scala.xml.MetaData.value"),
        ProblemFilters.exclude[ReversedMissingMethodProblem]("scala.xml.NodeSeq.theSeq"),

        // Synthetic static accessors (for Java interop) have a changed return type
        ProblemFilters.exclude[IncompatibleResultTypeProblem]("scala.xml.Null.apply"),
        ProblemFilters.exclude[IncompatibleResultTypeProblem]("scala.xml.Null.value"),

        // used to be a declared method, now a bridge without generic signature
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.MetaData.apply"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Null.apply"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Null.value"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.PrefixedAttribute.apply"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.PrefixedAttribute.value"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.UnprefixedAttribute.apply"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.UnprefixedAttribute.value"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Document.theSeq"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Group.theSeq"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Node.theSeq"),

        // Option[c.Seq] => Option[i.Seq] results in a changed generic signature
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.MetaData.get"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Null.get"),
        ProblemFilters.exclude[IncompatibleSignatureProblem]("scala.xml.Node.attribute"),

        // trait Attribute now extends trait ScalaVersionSpecificMetaData to ensure the previous signatures
        // with return type `collection.Seq` remain valid.
        // (trait Attribute extends MetaData, but that parent is not present in bytecode because it's a class.)
        ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("scala.xml.Attribute.apply"),
      ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq( // Scala 3-specific exclusions
        )
        case Some((2, minor)) => Seq( // Scala 2-specific exclusions
        ) ++ (minor match {
          case 13 => Seq( // Scala 2.13-specific exclusions
          )
          case 12 => Seq( // Scala 2.12-specific exclusions
          )
        })
        case _ => Seq()
      })
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
    libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.17.0" % Test,
    libraryDependencies += "xerces" % "xercesImpl" % "2.12.2" % Test,
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Seq()
      case _ =>
        Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value % Test)
    }),
  )
  .jvmEnablePlugins(SbtOsgi)
  .jsSettings(
    versionPolicyCheck / skip := true,
    versionCheck       / skip := true,
    // Scala.js cannot run forked tests
    Test / fork := false
  )
  .jsEnablePlugins(ScalaJSJUnitPlugin)
  .nativeSettings(
    versionPolicyCheck / skip := true,
    versionCheck       / skip := true,
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
        .fold(w => throw w.resolveException, identity)
      val jarPath = cp
        .find(_.toString.contains("junit-plugin"))
        .getOrElse(throw new Exception("Can't find Scala Native junit-plugin jar"))
      s"-Xplugin:$jarPath"
    },
    Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-s", "-v"),
  )
