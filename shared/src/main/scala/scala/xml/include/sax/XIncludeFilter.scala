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
package include.sax

import scala.xml.include._
import xml.Nullables._
import org.xml.sax.{Attributes, Locator, XMLReader}
import org.xml.sax.helpers.{AttributesImpl, NamespaceSupport, XMLFilterImpl, XMLReaderFactory}

import java.io.{BufferedInputStream, IOException, InputStreamReader, UnsupportedEncodingException}
import java.net.{MalformedURLException, URL, URLConnection}

/**
 * This is a SAX filter which resolves all XInclude include elements before
 *  passing them on to the client application. Currently this class has the
 *  following known deviation from the XInclude specification:
 *
 *  1. XPointer is not supported.
 *
 *  Furthermore, I would definitely use a new instance of this class for each
 *  document you want to process. I doubt it can be used successfully on
 *  multiple documents. Furthermore, I can virtually guarantee that this
 *  class is not thread safe. You have been warned.
 *
 *  Since this class is not designed to be subclassed, and since I have not
 *  yet considered how that might affect the methods herein or what other
 *  protected methods might be needed to support subclasses, I have declared
 *  this class final. I may remove this restriction later, though the use-case
 *  for subclassing is weak. This class is designed to have its functionality
 *  extended via a horizontal chain of filters, not a vertical hierarchy of
 *  sub and superclasses.
 *
 *  To use this class:
 *
 *  - Construct an `XIncludeFilter` object with a known base URL
 *  - Pass the `XMLReader` object from which the raw document will be read to
 *    the `setParent()` method of this object.
 *  - Pass your own `ContentHandler` object to the `setContentHandler()`
 *    method of this object. This is the object which will receive events
 *    from the parsed and included document.
 *  - Optional: if you wish to receive comments, set your own `LexicalHandler`
 *    object as the value of this object's
 *    `http://xml.org/sax/properties/lexical-handler` property.
 *    Also make sure your `LexicalHandler` asks this object for the status of
 *    each comment using `insideIncludeElement` before doing anything with the
 *    comment.
 *  - Pass the URL of the document to read to this object's `parse()` method
 *
 *  e.g.
 *  {{{
 *  val includer = new XIncludeFilter(base)
 *  includer setParent parser
 *  includer setContentHandler new SAXXIncluder(System.out)
 *  includer parse args(i)
 *  }}}
 *  translated from Elliotte Rusty Harold's Java source.
 *
 * @author Burak Emir
 */
class XIncludeFilter extends XMLFilterImpl {

  final val XINCLUDE_NAMESPACE: String = "http://www.w3.org/2001/XInclude"

  private val bases: java.util.Stack[URL] = new java.util.Stack[URL]()
  private val locators: java.util.Stack[Locator] = new java.util.Stack[Locator]()

  /*    private EntityResolver resolver;

    public XIncludeFilter() {
        this(null);
    }

    public XIncludeFilter(EntityResolver resolver) {
        this.resolver = resolver;
    }   */

  // what if this isn't called????
  // do I need to check this in startDocument() and push something
  // there????
  override def setDocumentLocator(locator: Locator): Unit = {
    locators push locator
    val base: String = locator.getSystemId
    try {
      bases.push(new URL(base))
    } catch {
      case _: MalformedURLException =>
        throw new UnsupportedOperationException(s"Unrecognized SYSTEM ID: $base")
    }
    super.setDocumentLocator(locator)
  }

  // necessary to throw away contents of non-empty XInclude elements
  private var level: Int = 0

  /**
   * This utility method returns true if and only if this reader is
   * currently inside a non-empty include element. (This is '''not''' the
   * same as being inside the node set which replaces the include element.)
   * This is primarily needed for comments inside include elements.
   * It must be checked by the actual `LexicalHandler` to see whether
   * a comment is passed or not.
   *
   * @return boolean
   */
  def insideIncludeElement: Boolean = level != 0

  override def startElement(uri: String, localName: String, qName: String, atts1: Attributes): Unit = {
    var atts: Attributes = atts1
    if (level == 0) { // We're not inside an xi:include element

      // Adjust bases stack by pushing either the new
      // value of xml:base or the base of the parent
      val base: String = atts.getValue(NamespaceSupport.XMLNS, "base")
      val parentBase: URL = bases.peek()
      var currentBase: URL = parentBase
      if (base != null) {
        try {
          currentBase = new URL(parentBase, base)
        } catch {
          case e: MalformedURLException =>
            throw new SAXException(s"Malformed base URL: $currentBase", e)
        }
      }
      bases push currentBase

      if (uri == XINCLUDE_NAMESPACE && localName == "include") {
        // include external document
        val href: String = atts.getValue("href")
        // Verify that there is an href attribute
        if (href == null) {
          throw new SAXException("Missing href attribute")
        }

        var parse: String = atts.getValue("parse")
        if (parse == null) parse = "xml"

        if (parse == "text")
          includeTextDocument(href, atts.getValue("encoding"))
        else if (parse == "xml")
          includeXMLDocument(href)
        // Need to check this also in DOM and JDOM????
        else
          throw new SAXException(s"Illegal value for parse attribute: $parse")
        level += 1
      } else {
        if (atRoot) {
          // add xml:base attribute if necessary
          val attsImpl: AttributesImpl = new AttributesImpl(atts)
          attsImpl.addAttribute(NamespaceSupport.XMLNS, "base",
            "xml:base", "CDATA", currentBase.toExternalForm)
          atts = attsImpl
          atRoot = false
        }
        super.startElement(uri, localName, qName, atts)
      }
    }
  }

  override def endElement(uri: String, localName: String, qName: String): Unit = {
    if (uri == XINCLUDE_NAMESPACE && localName == "include") {
      level -= 1
    } else if (level == 0) {
      bases.pop()
      super.endElement(uri, localName, qName)
    }
  }

  private var depth: Int = 0

  override def startDocument(): Unit = {
    level = 0
    if (depth == 0) super.startDocument()
    depth += 1
  }

  override def endDocument(): Unit = {
    locators.pop()
    bases.pop() // pop the URL for the document itself
    depth -= 1
    if (depth == 0) super.endDocument()
  }

  // how do prefix mappings move across documents????
  override def startPrefixMapping(prefix: String, uri: String): Unit = {
    if (level == 0) super.startPrefixMapping(prefix, uri)
  }

  override def endPrefixMapping(prefix: String): Unit = {
    if (level == 0) super.endPrefixMapping(prefix)
  }

  override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
    if (level == 0) super.characters(ch, start, length)
  }

  override def ignorableWhitespace(ch: Array[Char], start: Int, length: Int): Unit = {
    if (level == 0) super.ignorableWhitespace(ch, start, length)
  }

  override def processingInstruction(target: String, data: String): Unit = {
    if (level == 0) super.processingInstruction(target, data)
  }

  override def skippedEntity(name: String): Unit = {
    if (level == 0) super.skippedEntity(name)
  }

  // convenience method for error messages
  private def getLocation: String = {
    var locationString: String = ""
    val locator: Locator = locators.peek
    var publicID: String = ""
    var systemID: String = ""
    var column: Int = -1
    var line: Int = -1
    if (locator != null) {
      publicID = locator.getPublicId
      systemID = locator.getSystemId
      line = locator.getLineNumber
      column = locator.getColumnNumber
    }
    locationString = s" in document included from $publicID at $systemID at line $line, column $column"

    locationString
  }

  /**
   * This utility method reads a document at a specified URL and fires off
   * calls to `characters()`. It's used to include files with `parse="text"`.
   *
   * @param  url          URL of the document that will be read
   * @param  encoding1    Encoding of the document; e.g. UTF-8,
   *                      ISO-8859-1, etc.
   * @return void
   * @throws SAXException if the requested document cannot
   * be downloaded from the specified URL
   * or if the encoding is not recognized
   */
  private def includeTextDocument(url: String, encoding1: String): Unit = {
    var encoding: String = encoding1
    if (encoding == null || encoding.trim.isEmpty) encoding = "UTF-8"
    var source: Nullable[URL] = null
    try {
      val base: URL = bases.peek
      source = new URL(base, url)
    } catch {
      case e: MalformedURLException =>
        val ex: UnavailableResourceException = new UnavailableResourceException(s"Unresolvable URL $url$getLocation")
        ex.setRootCause(e)
        throw new SAXException(s"Unresolvable URL $url$getLocation", ex)
    }

    try {
      val uc: URLConnection = source.nn.openConnection
      val in: BufferedInputStream = new BufferedInputStream(uc.getInputStream)
      val encodingFromHeader: String = uc.getContentEncoding
      var contentType: String = uc.getContentType
      if (encodingFromHeader != null)
        encoding = encodingFromHeader
      else {
        // What if file does not have a MIME type but name ends in .xml????
        // MIME types are case-insensitive
        // Java may be picking this up from file URL
        if (contentType != null) {
          contentType = contentType.toLowerCase
          if (contentType == "text/xml"
            || contentType == "application/xml"
            || (contentType.startsWith("text/") && contentType.endsWith("+xml"))
            || (contentType.startsWith("application/") && contentType.endsWith("+xml"))) {
            encoding = EncodingHeuristics.readEncodingFromStream(in)
          }
        }
      }
      val reader: InputStreamReader = new InputStreamReader(in, encoding)
      val c: Array[Char] = new Array[Char](1024)
      var charsRead: Int = 0 // bogus init value
      while ({ {
        charsRead = reader.read(c, 0, 1024)
        if (charsRead > 0) this.characters(c, 0, charsRead)
      } ; charsRead != -1}) ()
    } catch {
      case e: UnsupportedEncodingException =>
        throw new SAXException(s"Unsupported encoding: $encoding$getLocation", e)
      case e: IOException =>
        throw new SAXException(s"Document not found: ${source.nn.toExternalForm}$getLocation", e)
    }
  }

  private var atRoot: Boolean = false

  /**
   * This utility method reads a document at a specified URL
   * and fires off calls to various `ContentHandler` methods.
   * It's used to include files with `parse="xml"`.
   *
   * @param  url          URL of the document that will be read
   * @return void
   * @throws SAXException if the requested document cannot
   * be downloaded from the specified URL.
   */
  private def includeXMLDocument(url: String): Unit = {
    val source: URL =
      try new URL(bases.peek, url)
      catch {
        case e: MalformedURLException =>
          val ex: UnavailableResourceException = new UnavailableResourceException(s"Unresolvable URL $url$getLocation")
          ex setRootCause e
          throw new SAXException(s"Unresolvable URL $url$getLocation", ex)
      }

    try {
      val parser: XMLReader =
        try XMLReaderFactory.createXMLReader()
        catch {
          case _: SAXException =>
            try XMLReaderFactory.createXMLReader(XercesClassName)
            catch { case _: SAXException => return System.err.println("Could not find an XML parser") }
        }

      parser setContentHandler this
      val resolver: EntityResolver = this.getEntityResolver
      if (resolver != null)
        parser setEntityResolver resolver

      // save old level and base
      val previousLevel: Int = level
      this.level = 0
      if (bases.contains(source))
        throw new SAXException(
          "Circular XInclude Reference",
          new CircularIncludeException(s"Circular XInclude Reference to $source$getLocation")
        )

      bases push source
      atRoot = true
      parser parse source.toExternalForm

      // restore old level and base
      this.level = previousLevel
      bases.pop()
    } catch {
      case e: IOException =>
        throw new SAXException(s"Document not found: ${source.toExternalForm}$getLocation", e)
    }
  }
}
