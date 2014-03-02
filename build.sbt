scalaModuleSettings

name                       := "scala-xml"

version                    := "1.0.0-SNAPSHOT"

scalaVersion               := "2.11.0-M8"

snapshotScalaBinaryVersion := "2.11.0-M8"

// important!! must come here (why?)
scalaModuleOsgiSettings

OsgiKeys.exportPackage := Seq(s"scala.xml.*;version=${version.value}")

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

// used in CompilerErrors test
// used in CompilerErrors test
libraryDependencies += ("org.scala-lang" % "scala-compiler" % scalaVersion.value % "test").exclude("org.scala-lang.modules", s"scala-xml_${scalaBinaryVersion.value}")

fork in Test := true

