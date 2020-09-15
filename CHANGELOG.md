# Scala XML Changes

## 2.0.0-M2 (2020-09-15)

Published for Scala 2.12 and 2.13, Scala.js 1.2.0,
and Dotty 0.27.0-RC1.

### Removed

- Removed `scala.xml.dtd.ElementValidator`

## 2.0.0-M1 (2019-10-21)

Not binary compatible with Scala XML 1.2.0.

Published for Scala 2.12, 2.13 and Scala.js 0.6, 1.0.0-M8.
Artifacts are no longer published for Scala 2.11.

Some deprecated elements have been removed; see the "[Removed](#Removed)" section below.

### Added

- The `apiURL` is now published in ivy metadata so that hyperlinks
  exist in downstream projects that reference Scala XML in their
  Scaladocs.

### Changed

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

The deletions represent about one thousand lines of code (sloc).  By
comparison Scala XML is 10,000 sloc, so this is about 10% reduction in
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
