# Scala XML Changes

## 2.0.0 (2021-05-13)

Not binary compatible with Scala XML 1.3.0.

Published for Scala 2.12, 2.13, and 3.0, Scala.js 1.5,
and Scala Native 0.4.

Artifacts are no longer published for Scala 2.11 and Scala.js 0.6.

A number of deprecated elements have been removed from the library;
see the "[Removed](#Removed)" section below.  The library's JAR byte
size is about 15% smaller.

### Added

- Add `scala.xml.transform.NestingTransformer`, to apply a single rule
  recursively, to give the original behavior of `RuleTransformer`, see
  below.
- The `apiURL` is now published in ivy metadata so that hyperlinks
  exist in downstream projects that reference Scala XML in their
  Scaladocs.
- Declare version policy of with early-semver in Mima with
  sbt-version-policy plugin

### Changed

- Changes to the default parser settings for the JDK SAXParser, see
  [Safe parser defaults](https://github.com/scala/scala-xml/wiki/Safer-parser-defaults)
  page on the wiki.
- The parser used by the load methods from `scala.xml.XML` and from
  `scala.xml.factory.XMLLoader` is now a `ThreadLocal` instance of
  SAXParser to reuse the parser instance and avoid repeatedly
  allocating one on every file load.
- Improve `scala.xml.transform.RuleTransformer` to apply all rules recursively.
- Reject invalid comment text that ends in a dash (-) in `scala.xml.Comment`.
- Changed use of `scala.collection.mutable.Stack` in `FactoryAdapter` to a
  `scala.collection.immutable.List`.  These members were affected.
  - `attribStack`
  - `hStack`
  - `tagStack`
  - `scopeStack`
- The abstract class `FactoryAdapter`, see above, is used elsewhere
  within the library, as well, so the previous changes are also
  inherited by:
    - `scala.xml.parsing.NoBindingFactoryAdapter` implemented class
    - `scala.xml.factory.XMLLoader.adapter` static member

### Fixed

- Attribute order is preserved for XML elements, not reversed.
- Don't escape quotes in `scala.xml.PCData` and `CDATA` as an XML `&quot;`

### Removed

Most of these deletions are of vestigial code that is either unused,
of poor quality or both.  Very few users of Scala XML will even notice
the removed parts.  Most users will not be affected.

The deletions represent about 1500 lines of code (sloc).  By
comparison Scala XML is 10,000 sloc, so this is about 15% reduction in
sloc.  The code that supports XML literals is maintained upstream in
the Scala compiler, not in the Scala XML library.

- Remove deprecated `scala.xml.pull.XMLEventReader`
- Remove deprecated versions of `scala.xml.Elem` constructors
- Remove deprecated `scala.xml.Elem.xmlToProcess` and
  `scala.xml.Elem.processXml`
- Remove deprecated definitions under `scala.xml.persistent`
  - `CachedFileStorage`
  - `Index`
  - `SetStorage`
- Remove `scala.xml.dtd.impl.PointedHedgeExp`
- Remove `scala.xml.dtd.Scanner`
- Remove `scala.xml.dtd.ContentModelParser`
- Remove `scala.xml.dtd.ElementValidator`
- Remove `scala.xml.factory.Binder`
- Remove `scala.xml.parsing.ValidatingMarkupHandler`
- Remove `scala.xml.Properties`
- Remove `scala.xml.factory.LoggedNodeFactory`
- Remove `scala.xml.parsing.MarkupHandler.log`
