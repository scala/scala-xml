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
package xml
package parsing

import scala.collection.Seq
import org.xml.sax.{Attributes, Locator, SAXNotRecognizedException, SAXNotSupportedException}
import org.xml.sax.ext.{DefaultHandler2, Locator2}

// can be mixed into FactoryAdapter if desired
trait ConsoleErrorHandler extends DefaultHandler2 {
  // ignore warning, crimson warns even for entity resolution!
  override def warning(ex: SAXParseException): Unit = ()
  override def error(ex: SAXParseException): Unit = printError("Error", ex)
  override def fatalError(ex: SAXParseException): Unit = printError("Fatal Error", ex)

  protected def printError(errtype: String, ex: SAXParseException): Unit =
    Console.withOut(Console.err) {
      val s: String = "[%s]:%d:%d: %s".format(
        errtype, ex.getLineNumber, ex.getColumnNumber, ex.getMessage)
      Console.println(s)
      Console.flush()
    }
}

/**
 * SAX adapter class, for use with Java SAX parser. Keeps track of
 *  namespace bindings, without relying on namespace handling of the
 *  underlying SAX parser (but processing the parser's namespace-related events if it is namespace-aware).
 */
abstract class FactoryAdapter extends DefaultHandler2 with factory.XMLLoader[Node] {
  val normalizeWhitespace: Boolean = false

  // reference to the XMLReader that parses the document; this is used to query
  // features (e.g., 'is-standalone') and properties (e.g., document-xml-version) -
  // see http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html
  private var xmlReader: Option[XMLReader] = None

  private var dtdBuilder: Option[DtdBuilder] = None
  private def inDtd: Boolean = dtdBuilder.isDefined && !dtdBuilder.get.isDone

  private var document: Option[Document] = None
  private var baseURI: Option[String] = None
  private var xmlEncoding: Option[String] = None

  private var prefixMappings: List[(String, String)] = List.empty

  // TODO all the variables should be private, but - binary compatibility...
  var prolog: List[Node] = List.empty
  var rootElem: Node = _
  var epilogue: List[Node] = List.empty

  val buffer: StringBuilder = new StringBuilder()
  private var inCDATA: Boolean = false

  /** List of attributes
    * 
    * Previously was a mutable [[scala.collection.mutable.Stack Stack]], but is now a mutable reference to an immutable [[scala.collection.immutable.List List]].
    * 
    * @since 2.0.0 
    */
  var attribStack: List[MetaData] = List.empty
  /** List of elements
    * 
    * Previously was a mutable [[scala.collection.mutable.Stack Stack]], but is now a mutable reference to an immutable [[scala.collection.immutable.List List]].
    * 
    * @since 2.0.0 
    */
  var hStack: List[Node] = List.empty // [ element ] contains siblings
  /** List of element names
    * 
    * Previously was a mutable [[scala.collection.mutable.Stack Stack]], but is now a mutable reference to an immutable [[scala.collection.immutable.List List]].
    * 
    * @since 2.0.0 
    */
  var tagStack: List[String] = List.empty
  /** List of namespaces
    * 
    * Previously was a mutable [[scala.collection.mutable.Stack Stack]], but is now a mutable reference to an immutable [[scala.collection.immutable.List List]].
    * 
    * @since 2.0.0 
    */
  var scopeStack: List[NamespaceBinding] = List.empty

  var curTag: String = _
  var capture: Boolean = false

  /**
   * Captures text or cdata.
   */
  def captureText(): Unit = {
    if (capture && buffer.nonEmpty) {
      val text: String = buffer.toString
      val newNode: Node = if (inCDATA) createPCData(text) else createText(text)
      hStack ::= newNode
    }

    buffer.clear()
    inCDATA = false
  }

  /**
   * Load XML document from the inputSource using the xmlReader.
   */
  def loadDocument(inputSource: InputSource, xmlReader: XMLReader): Document = {
    if (inputSource == null) throw new IllegalArgumentException("InputSource cannot be null")

    xmlReader.setContentHandler(this)
    xmlReader.setDTDHandler(this)
    /* Do not overwrite pre-configured EntityResolver. */
    if (xmlReader.getEntityResolver == null) xmlReader.setEntityResolver(this)
    /* Do not overwrite pre-configured ErrorHandler. */
    if (xmlReader.getErrorHandler == null) xmlReader.setErrorHandler(this)

    /* Use LexicalHandler if it is supported by the xmlReader. */
    try {
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", this)
    } catch {
      case _: SAXNotRecognizedException =>
      case _: SAXNotSupportedException =>
    }

    /* Use DeclHandler if it is supported by the xmlReader. */
    try {
      xmlReader.setProperty("http://xml.org/sax/properties/declaration-handler", this)
    } catch {
      case _: SAXNotRecognizedException =>
      case _: SAXNotSupportedException =>
    }

    this.xmlReader = Some(xmlReader)
    xmlReader.parse(inputSource)

    document.get
  }

  // abstract methods

  /**
   * Tests if an XML element contains text.
   * @return true if element named `localName` contains text.
   */
  def nodeContainsText(localName: String): Boolean // abstract

  /**
   * creates an new non-text(tree) node.
   * @param elemName
   * @param attribs
   * @param chIter
   * @return a new XML element.
   */
  def createNode(pre: String, elemName: String, attribs: MetaData,
                 scope: NamespaceBinding, chIter: List[Node]): Node // abstract

  /**
   * creates a Text node.
   * @param text
   * @return a new Text node.
   */
  def createText(text: String): Text // abstract

  /**
   * creates a PCData node.
   * @param text
   * @return a new PCData node.
   */
  def createPCData(text: String): PCData // abstract

  /**
   * creates a new processing instruction node.
   */
  def createProcInstr(target: String, data: String): Seq[ProcInstr]

  /**
   * creates a new comment node.
   */
  def createComment(characters: String): Seq[Comment]

  /* ContentHandler methods */

  // Since Java 14, ContentHandler has a method that delivers the values from the XML declaration:
  //   def declaration(version: String, encoding: String, standalone: String): Unit = ()
  // but it'll be years until we are all on Java 14 *and* Xerces starts calling this method...

  override def setDocumentLocator(locator: Locator): Unit = {
    baseURI = Option(locator.getSystemId)
    locator match {
      case locator2: Locator2 =>
        // Note: Xerces calls setDocumentLocator() (and startDocument()) *before* it even reads the XML declaration;
        // the version delivered here - locator2.getXMLVersion - is always "1.0";
        // the real version is retrieved as a property of the XML reader in endDocument().

        xmlEncoding = Option(locator2.getEncoding)
      case _ =>
    }
  }

  override def startDocument(): Unit = {
    scopeStack ::= TopScope // TODO turn into a parameter
  }

  override def endDocument(): Unit = {
    // capture the epilogue at the end of the document
    epilogue = hStack.init.reverse

    val document = new Document
    this.document = Some(document)
    document.children = prolog ++ rootElem ++ epilogue
    document.docElem = rootElem
    document.dtd = dtdBuilder.map(_.dtd).orNull
    document.baseURI = baseURI.orNull
    document.encoding = xmlEncoding

    document.version =
      try {
        Option(xmlReader.get.getProperty("http://xml.org/sax/properties/document-xml-version").asInstanceOf[String])
      } catch {
        case _: SAXNotRecognizedException => None
        case _: SAXNotSupportedException => None
      }

    document.standAlone =
      try {
        Some(xmlReader.get.getFeature("http://xml.org/sax/features/is-standalone"))
      } catch {
        case _: SAXNotRecognizedException => None
        case _: SAXNotSupportedException => None
      }

    // Note: resetting to the freshly-created state; needed only if this instance is reused, which we do not do...
    dtdBuilder = None
    xmlReader = None

    baseURI = None
    xmlEncoding = None

    hStack = hStack.last :: Nil // TODO List.empty
    scopeStack = scopeStack.tail // TODO List.empty

    rootElem = null
    prolog = List.empty
    epilogue = List.empty

    buffer.clear()
    inCDATA = false
    capture = false
    curTag = null

    attribStack = List.empty
    tagStack = List.empty
  }

  override def startPrefixMapping(prefix: String, uri: String): Unit =
    prefixMappings ::= ((prefix, uri))

  override def endPrefixMapping(prefix: String): Unit = ()

  /* Start element. */
  override def startElement(
    uri: String,
    _localName: String,
    qname: String,
    attributes: Attributes
  ): Unit = {
    captureText()

    // capture the prolog at the start of the root element
    if (tagStack.isEmpty) {
      prolog = hStack.reverse
      hStack = List.empty
    }

    tagStack ::= curTag
    curTag = qname

    val localName: String = Utility.splitName(qname)._2
    capture = nodeContainsText(localName)

    hStack ::= null
    var m: MetaData = Null
    var scpe: NamespaceBinding =
      if (scopeStack.isEmpty) TopScope
      else scopeStack.head

    for (i <- 0.until(attributes.getLength).reverse) {
      val qname: String = attributes.getQName(i)
      val value: String = attributes.getValue(i)
      val (pre: Option[String], key: String) = Utility.splitName(qname)
      def nullIfEmpty(s: String): String = if (s == "") null else s

      if (pre.contains("xmlns") || (pre.isEmpty && qname == "xmlns")) {
        val arg: String = if (pre.isEmpty) null else key
        scpe = NamespaceBinding(arg, nullIfEmpty(value), scpe)
      } else
        m = Attribute(pre, key, Text(value), m)
    }

    // Add namespace bindings for the prefix mappings declared by this element
    // (if there are any, the parser is namespace-aware, and no namespace bindings were delivered as attributes).
    // All `startPrefixMapping()` events will occur immediately before the corresponding `startElement()` event.
    for ((prefix: String, uri: String) <- prefixMappings)
      scpe = NamespaceBinding(if (prefix.isEmpty) null else prefix, uri, scpe)

    // Once the `prefixMappings` are processed into `scpe`, the list is emptied out
    // so that already-declared namespaces are not re-declared on the nested elements.
    prefixMappings = List.empty

    scopeStack ::= scpe
    attribStack ::=  m
  }

  /**
   * End element.
   * @param uri
   * @param _localName
   * @param qname
   * @throws org.xml.sax.SAXException if ..
   */
  override def endElement(uri: String, _localName: String, qname: String): Unit = {
    captureText()
    val metaData: MetaData = attribStack.head
    attribStack = attribStack.tail

    // reverse order to get it right
    val v: List[Node] = hStack.takeWhile(_ != null).reverse
    hStack = hStack.dropWhile(_ != null) match {
      case null :: hs => hs
      case hs => hs
    }
    val (pre: Option[String], localName: String) = Utility.splitName(qname)
    val scp: NamespaceBinding = scopeStack.head
    scopeStack = scopeStack.tail

    // create element
    rootElem = createNode(pre.orNull, localName, metaData, scp, v)
    hStack ::= rootElem
    curTag = tagStack.head
    tagStack = tagStack.tail
    capture = curTag != null && nodeContainsText(curTag) // root level
  }

  /**
   * Capture characters, possibly normalizing whitespace.
   *
   * @param ch
   * @param offset
   * @param length
   */
  override def characters(ch: Array[Char], offset: Int, length: Int): Unit = {
    if (!capture) ()
    // compliant: report every character
    else if (!normalizeWhitespace) buffer.appendAll(ch, offset, length)
    // normalizing whitespace is not compliant, but useful
    else {
      var it: Iterator[Char] = ch.slice(offset, offset + length).iterator
      while (it.hasNext) {
        val c: Char = it.next()
        val isSpace: Boolean = c.isWhitespace
        buffer.append(if (isSpace) ' ' else c)
        if (isSpace)
          it = it.dropWhile(_.isWhitespace)
      }
    }
  }

  override def ignorableWhitespace(ch: Array[Char], offset: Int, length: Int): Unit = ()

  /**
   * Processing instruction.
   */
  override def processingInstruction(target: String, data: String): Unit =
    if (inDtd) dtdBuilder.foreach(_.processingInstruction(target, data)) else {
      captureText()
      hStack = hStack.reverse_:::(createProcInstr(target, data).toList)
    }

  override def skippedEntity(name: String): Unit = ()

  /* LexicalHandler methods (see https://docs.oracle.com/javase/8/docs/api/org/xml/sax/ext/LexicalHandler.html) */

  override def startDTD(
    name: String,
    publicId: String,
    systemId: String
  ): Unit = dtdBuilder = Some(DtdBuilder(
    name,
    publicId,
    systemId
  ))

  override def endDTD(): Unit = dtdBuilder.foreach(_.endDTD())

  override def startEntity(name: String): Unit = dtdBuilder.foreach(_.startEntity(name))
  override def endEntity(name: String): Unit = dtdBuilder.foreach(_.endEntity(name))

  /**
   * Start of a CDATA section.
   */
  override def startCDATA(): Unit = {
    captureText()
    inCDATA = true
  }

  /**
   * End of a CDATA section.
   */
  override def endCDATA(): Unit = captureText()

  /**
   * Comment.
   */
  override def comment(ch: Array[Char], start: Int, length: Int): Unit = {
    val commentText: String = String.valueOf(ch.slice(start, start + length))
    if (inDtd) dtdBuilder.foreach(_.comment(commentText)) else {
      captureText()
      hStack = hStack.reverse_:::(createComment(commentText).toList)
    }
  }

  /* DTDHandler methods (see https://docs.oracle.com/javase/8/docs/api/org/xml/sax/DTDHandler.html) */

  override def notationDecl(name: String, publicId: String, systemId: String): Unit =
    dtdBuilder.foreach(_.notationDecl(name, publicId, systemId))

  override def unparsedEntityDecl(name: String, publicId: String, systemId: String, notationName: String): Unit =
    dtdBuilder.foreach(_.unparsedEntityDecl(name, publicId, systemId, notationName))

  /* DeclHandler methods (see https://docs.oracle.com/javase/8/docs/api/org/xml/sax/ext/DeclHandler.html) */

  override def elementDecl(name: String, model: String): Unit =
    dtdBuilder.foreach(_.elementDecl(name, model))

  override def attributeDecl(eName: String, aName: String, `type`: String, mode: String, value: String): Unit =
    dtdBuilder.foreach(_.attributeDecl(eName, aName, `type`, mode, value))

  override def internalEntityDecl(name: String, value: String): Unit =
    dtdBuilder.foreach(_.internalEntityDecl(name, value))

  override def externalEntityDecl(name: String, publicId: String, systemId: String): Unit =
    dtdBuilder.foreach(_.externalEntityDecl(name, publicId, systemId))
}
