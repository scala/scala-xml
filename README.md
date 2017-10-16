scala-xml
[![Travis](https://img.shields.io/travis/scala/scala-xml.svg)](https://travis-ci.org/scala/scala-xml)
[![latest release for 2.11](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.11.svg?label=scala+2.11)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.11)
[![latest release for 2.12](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.12.svg?label=scala+2.12)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.12)
[![latest release for 2.13.0-M2](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.13.0-M2.svg?label=scala+2.13.0-M2)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.13.0-M2)
[![Gitter](https://badges.gitter.im/Join+Chat.svg)](https://gitter.im/scala/scala-xml)
=========

The standard Scala XML library. Please file XML issues here, not at https://github.com/scala/bug/issues.

Since Scala 2.11, this library is a separate jar that can be omitted from Scala projects that do not use XML.

The decoupling works because the compiler desugars XML literals in Scala source code into a set of method calls. Alternative implementations of these calls are welcome! (The calls are unfortunately only defined by the [implementation](https://github.com/scala/scala/blob/2.11.x/src/compiler/scala/tools/nsc/ast/parser/SymbolicXMLBuilder.scala).)

API documentation is available [here](http://www.scala-lang.org/api/current/scala-xml/scala/xml/index.html).

How to documentation is available in the [wiki](https://github.com/scala/scala-xml/wiki)

## Cross-building with 2.10

If you are cross-building a project that uses scala-xml with both Scala 2.10 and later Scala versions, take a look at [this example](https://github.com/scala/scala-module-dependency-sample).

## Maintenance status

This library is community-maintained. The lead maintainers are [@aaron_s_hawley](https://github.com/ashawley) and [@biswanaths](https://github.com/biswanaths).

## Security best practices

The XML spec has some features that are best turned off, to avoid unsavory things like file system access, DoS attacks,... Issue [#17](https://github.com/scala/scala-xml/issues/17) tracks the recommended way of configuring the XML parser used by scala-xml to avoid these. This is by no means an exhaustive list. We'll be happy to incorporate your suggestions -- just comment on the ticket!
