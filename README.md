scala-xml [<img src="https://api.travis-ci.org/scala/scala-xml.png"/>](https://travis-ci.org/scala/scala-xml)
=========

The standard Scala XML library.

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use XML.
We're also looking forward to alternative implementations!

API documentation is available [here](http://www.scala-lang.org/api/current/scala-xml/).

## Adding an SBT dependency
To depend on scala-xml in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
```

Maven users, or sbt users looking to supportmultiple Scala versions, please see the more elaborate example in https://github.com/scala/scala-module-dependency-sample.
