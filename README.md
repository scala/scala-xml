scala-xml
[![Travis](https://img.shields.io/travis/scala/scala-xml.svg)](https://travis-ci.org/scala/scala-xml)
[![latest release for 2.11](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.11.svg?label=scala+2.11)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.11)
[![latest release for 2.12](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.12.svg?label=scala+2.12)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.12)
[![latest release for 2.13](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.13.svg?label=scala+2.13)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.13)
[![Gitter](https://badges.gitter.im/Join+Chat.svg)](https://gitter.im/scala/scala-xml)
=========

The standard Scala XML library. Please file XML issues here, not at https://github.com/scala/bug/issues.

The decoupling of scala-xml from the Scala compiler and standard library is possible because the compiler desugars XML literals in Scala source code into a set of method calls. Alternative implementations of these calls are welcome! (The calls are unfortunately only defined by the [implementation](https://github.com/scala/scala/blob/2.11.x/src/compiler/scala/tools/nsc/ast/parser/SymbolicXMLBuilder.scala).)

API documentation is available [here](https://scala.github.io/scala-xml/api/1.2.0/scala/xml/).

How to documentation is available in the [wiki](https://github.com/scala/scala-xml/wiki)

The latest stable release of Scala XML is 1.3.0.

Experimental milestone releases of Scala XML version 2.0 are available, starting with 2.0.0-M1.  See the changes for 2.0 in `CHANGELOG.md`.

## Maintenance status

This library is community-maintained. The lead maintainer is [@aaron_s_hawley](https://github.com/ashawley).

Contributors are welcome, and should read the [contributor guide](https://github.com/scala/scala-xml/wiki/Contributor-guide) on the wiki.

## Issues

Many old issues from the Scala JIRA issue tracker have been migrated
here, but not all of them. Community assistance identifying and
migrating still-relevant issues is welcome.  See [this
page](https://github.com/scala/scala-xml/issues/62) for details.

## Security best practices

The XML spec has some features that are best turned off, to avoid unsavory things like file system access, DoS attacks,... Issue [#17](https://github.com/scala/scala-xml/issues/17) tracks the recommended way of configuring the XML parser used by scala-xml to avoid these. This is by no means an exhaustive list. We'll be happy to incorporate your suggestions -- just comment on the ticket!

## Related projects

- [Advxml](https://github.com/geirolz/advxml) - Functional library combining scala-xml with cats-core
- [Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala) - Reactive programming library
- [ezXML](https://github.com/JulienSt/ezXML) - Extensions for traverse, encoding, decoding and mapping XML
- [http4s-scala-xml](https://http4s.org/v0.21/entity/) - XML literal support in http4s
- [Json4s XML](https://github.com/json4s/json4s) - Conversion to and from JSON
- [monadic-html](https://github.com/OlivierBlanvillain/monadic-html) - DOM-like event-based programming with XHTML
- [scalaxb](http://scalaxb.org/) - XML data binding, serialization, SOAP and WSDL support
- [ScalaTags](https://github.com/lihaoyi/scalatags) - Alternative syntax for XML literals
- [scala-xml-dotty](https://github.com/felixmulder/scala-xml-dotty) - Macro library for XML literals in Dotty
- [xtract](https://github.com/lucidsoftware/xtract) - A library for deserializing XML
https://github.com/TinkoffCreditSystems/phobos
https://github.com/dylemma/xml-spac
https://github.com/ScalaWilliam/xs4s

See also the "XML" section of [Awesome Scala](https://github.com/lauris/awesome-scala).
