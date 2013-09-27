name := "scala-xml"

version := "1.0.0-SNAPSHOT"

// standard stuff follows:
scalaVersion := "2.11.0-M5"

// NOTE: not necessarily equal to scalaVersion
// (e.g., during PR validation, we override scalaVersion to validate,
// but don't rebuild scalacheck, so we don't want to rewire that dependency)
scalaBinaryVersion := "2.11.0-M5"

partestVersion := "1.0.0-RC6"

scalaModuleSettings

osgiSettings
 
val osgiVersion = version(_.replace('-', '.'))
 
OsgiKeys.bundleSymbolicName := s"${organization.value}.${name.value}"
 
OsgiKeys.bundleVersion := osgiVersion.value
 
OsgiKeys.exportPackage := Seq(s"scala.xml.*;version=${version.value}")
 
// Sources should also have a nice MANIFEST file
packageOptions in packageSrc := Seq(Package.ManifestAttributes(
                       ("Bundle-SymbolicName", s"${organization.value}.${name.value}.source"),
                       ("Bundle-Name", s"${name.value} sources"),
                       ("Bundle-Version", osgiVersion.value),
                       ("Eclipse-SourceBundle", s"""${organization.value}.${name.value};version="${osgiVersion.value}";roots:="."""")
                   ))

