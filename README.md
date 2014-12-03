scala-xml [<img src="https://api.travis-ci.org/scala/scala-xml.png"/>](https://travis-ci.org/scala/scala-xml)
=========

The standard Scala XML library. Not actively maintained.

Would you like to maintain this project? (Please open an issue/send an email!)

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use XML.

The compiler was decoupled from this particular implementation using the same approach as for comprehensions (xml syntax is desugared into a set of method calls, which unfortunately is only defined by the implementation). Alternative implementations are welcome!

API documentation is available [here](http://www.scala-lang.org/api/current/scala-xml/).

## Adding an SBT dependency
To depend on scala-xml in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
```

Maven users, or sbt users looking to support multiple Scala versions, please see the more elaborate example in https://github.com/scala/scala-module-dependency-sample.
