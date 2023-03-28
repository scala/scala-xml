scala-xml
[![latest release for 2.12](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.12.svg?label=scala+2.12)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.12)
[![latest release for 2.13](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_2.13.svg?label=scala+2.13)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_2.13)
[![latest release for 3.0](https://img.shields.io/maven-central/v/org.scala-lang.modules/scala-xml_3.svg?label=scala+3)](http://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml_3)
=========

The standard Scala XML library. Please file XML issues here, not at https://github.com/scala/bug/issues or http://github.com/lampepfl/dotty/issues.

The decoupling of scala-xml from the Scala compiler and standard library is possible because the compiler desugars XML literals in Scala source code into a set of method calls.
Alternative implementations of these calls are welcome!
Compiler code that shows the calls needed: 
  [Scala 2.11](https://github.com/scala/scala/blob/2.11.x/src/compiler/scala/tools/nsc/ast/parser/SymbolicXMLBuilder.scala),
  [Scala 2.12](https://github.com/scala/scala/blob/2.12.x/src/compiler/scala/tools/nsc/ast/parser/SymbolicXMLBuilder.scala),
  [Scala 2.13](https://github.com/scala/scala/blob/2.13.x/src/compiler/scala/tools/nsc/ast/parser/SymbolicXMLBuilder.scala),
  [Scala 3](https://github.com/lampepfl/dotty/blob/main/compiler/src/dotty/tools/dotc/parsing/xml/SymbolicXMLBuilder.scala).

API documentation is available [here](https://javadoc.io/doc/org.scala-lang.modules/scala-xml_2.13/).

How-to documentation is available in the [wiki](https://github.com/scala/scala-xml/wiki)

## Maintenance status

This library is community-maintained. Maintainers with merge rights include [@aaron_s_hawley](https://github.com/ashawley) and [@dubinsky](https://github.com/dubinsky).

Contributors are welcome. Please consult the [contributor guide](https://github.com/scala/scala-xml/wiki/Contributor-guide) on the wiki.

## Issues

Some old issues from the Scala issue tracker have been migrated
here, but not all of them. Community assistance identifying and
migrating still-relevant issues is welcome.  See [this
page](https://github.com/scala/scala-xml/issues/62) for details.

## Related projects

- [Advxml](https://github.com/geirolz/advxml) - Functional library combining scala-xml with cats-core
- [Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala) - Reactive programming library
- [ezXML](https://github.com/JulienSt/ezXML) - Extensions for traverse, encoding, decoding and mapping XML
- [http4s-scala-xml](https://http4s.github.io/http4s-scala-xml/) - XML literal support in http4s
- [Json4s XML](https://github.com/json4s/json4s) - Conversion to and from JSON
- [monadic-html](https://github.com/OlivierBlanvillain/monadic-html) - DOM-like event-based programming with XHTML
- [phobos](https://github.com/TinkoffCreditSystems/phobos) - Data-binding library based on stream parsing using Aalto XML
- [scalacheck-xml](https://github.com/typelevel/scalacheck-xml) - Provides Scalacheck instances for scala-xml
- [scalaxb](http://scalaxb.org/) - XML data binding, serialization, SOAP and WSDL support
- [ScalaTags](https://github.com/lihaoyi/scalatags) - Alternative syntax for XML literals
- [scala-xml-dotty](https://github.com/felixmulder/scala-xml-dotty) - Macro library for XML literals in Dotty
- [XML SPaC](https://github.com/dylemma/xml-spac) - Streaming event-based parser combinators
- [xs4s](https://github.com/ScalaWilliam/xs4s) - XML streaming for Scala
- [xtract](https://github.com/lucidsoftware/xtract) - A library for deserializing XML

You might also [search "XML" on Scaladex](https://index.scala-lang.org/search?q=xml).
