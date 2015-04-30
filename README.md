Maintainer wanted!
==================
Would you like to maintain this project? (Please open an issue/send an email!)

scala-xml [<img src="https://api.travis-ci.org/scala/scala-xml.png"/>](https://travis-ci.org/scala/scala-xml)
=========

The standard Scala XML library. Please file issues here instead of over at issues.scala-lang.org.

As of Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use XML.

The compiler was decoupled from this particular implementation using the same approach as for comprehensions (xml syntax is desugared into a set of method calls, which unfortunately is only defined by the [implementation](https://github.com/scala/scala/blob/2.11.x/src/compiler/scala/tools/nsc/ast/parser/SymbolicXMLBuilder.scala)). Alternative implementations are welcome!

API documentation is available [here](http://www.scala-lang.org/api/current/scala-xml/).

## Security best practices
The XML spec has some features that are best turned off, to avoid unsavory things like file system access, DoS attacks,... Issue [#17](https://github.com/scala/scala-xml/issues/17) tracks the recommended way of configuring the xml parser used by scala-xml to avoid these. This is by no means an exhaustive list. We'll be happy to incorporate your suggestions -- just comment on the ticket!

## Adding an SBT dependency
To depend on scala-xml in SBT, add something like this to your build.sbt:

```
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.4"
```

Maven users, or sbt users looking to support multiple Scala versions, please see the more elaborate example in https://github.com/scala/scala-module-dependency-sample.
