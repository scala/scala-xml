scala-xml [<img src="https://api.travis-ci.org/scala/scala-xml.png"/>](https://travis-ci.org/scala/scala-xml)
=========

The standard Scala XML library.

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use XML.
We're also looking forward to alternative implementations!

## Adding an SBT dependency
To depend on scala-xml in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
```

(Assuming you're using a `scalaVersion` for which a scala-xml is published.
The first 2.11 milestone for which this is true is 2.11.0-M4.)

To support multiple Scala versions, see the example in https://github.com/scala/scala-module-dependency-sample.
