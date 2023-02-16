/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala

/**
 * This library provides support for the XML literal syntax in Scala programs.
 * {{{
 * val planets: scala.xml.Elem = <planets>
 *   <planet id="earth">
 *     <title>Earth</title>
 *     <mass unit="kg">5.9742e24</mass>
 *     <radius unit="m">6378.14e3</radius>
 *   </planet>
 *   <planet id="mars">
 *     <title>Mars</title>
 *     <mass unit="kg">0.64191e24</mass>
 *     <radius unit="m">3397.0e3</radius>
 *   </planet>
 * </planets>
 * }}}
 *
 * Additionally, you can mix Scala expressions in your XML elements by
 * using the curly brace notation:
 *
 * {{{
 * val sunMass = 1.99e30
 * val sunRadius = 6.96e8
 * val star = <star>
 *   <title>Sun</title>
 *   <mass unit="kg">{ sunMass }</mass>
 *   <radius unit="m">{ sunRadius }</radius>
 *   <surface unit="m²">{ 4 * Math.PI * Math.pow(sunRadius, 2) }</surface>
 *   <volume unit="m³">{ 4/3 * Math.PI * Math.pow(sunRadius, 3) }</volume>
 * </star>
 * }}}
 *
 * An XML element, for example `<star/>` and `<planet/>`, is
 * represented in this library as a case class, [[scala.xml.Elem]].
 *
 * The sub-elements of XML values share a common base class,
 * [[scala.xml.Node]].
 *
 * However, the non-element declarations found in XML files share a
 * different common base class, [[scala.xml.dtd.Decl]].  Additionally,
 * document type declarations are represented by a different trait,
 * [[scala.xml.dtd.DTD]].
 *
 * For reading and writing XML data to and from files, see
 * [[scala.xml.XML]].  The default parser of XML data is the
 * [[http://xerces.apache.org/ Xerces]] parser and is provided in Java
 * by [[javax.xml.parsers.SAXParser]].
 *
 * For more control of the input, use the parser written in Scala that
 * is provided, [[scala.xml.parsing.ConstructingParser]].
 *
 * For working with XHTML input, use [[scala.xml.parsing.XhtmlParser]].
 *
 * For more control of the output, use the [[scala.xml.PrettyPrinter]].
 *
 * Utility methods for working with XML data are provided in
 * [[scala.xml.Utility]].
 *
 * XML values in Scala are immutable, but you can traverse and
 * transform XML data with a [[scala.xml.transform.RuleTransformer]].
 */
package object xml {
  val XercesClassName: String = "org.apache.xerces.parsers.SAXParser"

  type SAXException = org.xml.sax.SAXException
  type SAXParseException = org.xml.sax.SAXParseException
  type EntityResolver = org.xml.sax.EntityResolver
  type InputSource = org.xml.sax.InputSource
  type XMLReader = org.xml.sax.XMLReader
  type SAXParser = javax.xml.parsers.SAXParser
}
