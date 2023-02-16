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
package factory

import org.xml.sax.{SAXNotRecognizedException, XMLReader}
import javax.xml.parsers.SAXParserFactory
import parsing.{FactoryAdapter, NoBindingFactoryAdapter}
import java.io.{File, FileDescriptor, InputStream, Reader}
import java.net.URL

/**
 * Presents collection of XML loading methods which use the parser
 *  created by "def parser".
 */
trait XMLLoader[T <: Node] {
  import scala.xml.Source._
  def adapter: FactoryAdapter = new NoBindingFactoryAdapter()

  private lazy val parserInstance: ThreadLocal[SAXParser] = new ThreadLocal[SAXParser] {
    override def initialValue: SAXParser = {
      val parser: SAXParserFactory = SAXParserFactory.newInstance()
      parser.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true)
      parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
      parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
      parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
      parser.setFeature("http://xml.org/sax/features/external-general-entities", false)
      parser.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false)
      parser.setXIncludeAware(false)
      parser.setNamespaceAware(false)
      parser.newSAXParser()
    }
  }

  /* Override this to use a different SAXParser. */
  def parser: SAXParser = parserInstance.get

  /* Override this to use a different XMLReader. */
  def reader: XMLReader = parser.getXMLReader

  /**
   * Loads XML from the given InputSource, using the supplied parser.
   *  The methods available in scala.xml.XML use the XML parser in the JDK.
   */
  def loadXML(source: InputSource, parser: SAXParser): T = loadXML(source, parser.getXMLReader)

  def loadXMLNodes(source: InputSource, parser: SAXParser): Seq[Node] = loadXMLNodes(source, parser.getXMLReader)

  private def loadXML(source: InputSource, reader: XMLReader): T = {
    val result: FactoryAdapter = parse(source, reader)
    result.rootElem.asInstanceOf[T]
  }
  
  private def loadXMLNodes(source: InputSource, reader: XMLReader): Seq[Node] = {
    val result: FactoryAdapter = parse(source, reader)
    result.prolog ++ (result.rootElem :: result.epilogue)
  }

  private def parse(source: InputSource, reader: XMLReader): FactoryAdapter = {
    if (source == null) throw new IllegalArgumentException("InputSource cannot be null")

    val result: FactoryAdapter = adapter

    reader.setContentHandler(result)
    reader.setDTDHandler(result)
    /* Do not overwrite pre-configured EntityResolver. */
    if (reader.getEntityResolver == null) reader.setEntityResolver(result)
    /* Do not overwrite pre-configured ErrorHandler. */
    if (reader.getErrorHandler == null) reader.setErrorHandler(result)

    try {
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", result)
    } catch {
      case _: SAXNotRecognizedException =>
    }

    result.scopeStack = TopScope :: result.scopeStack
    reader.parse(source)
    result.scopeStack = result.scopeStack.tail

    result
  }

  /** loads XML from given InputSource. */
  def load(source: InputSource): T = loadXML(source, reader)

  /** Loads XML from the given file, file descriptor, or filename. */
  def loadFile(file: File): T = load(fromFile(file))
  def loadFile(fd: FileDescriptor): T = load(fromFile(fd))
  def loadFile(name: String): T = load(fromFile(name))

  /** loads XML from given InputStream, Reader, sysID, or URL. */
  def load(is: InputStream): T = load(fromInputStream(is))
  def load(reader: Reader): T = load(fromReader(reader))
  def load(sysID: String): T = load(fromSysId(sysID))
  def load(url: URL): T = load(fromInputStream(url.openStream()))

  /** Loads XML from the given String. */
  def loadString(string: String): T = load(fromString(string))

  /** Load XML nodes, including comments and processing instructions that precede and follow the root element. */
  def loadNodes(source: InputSource): Seq[Node] = loadXMLNodes(source, reader)
  def loadFileNodes(file: File): Seq[Node] = loadNodes(fromFile(file))
  def loadFileNodes(fd: FileDescriptor): Seq[Node] = loadNodes(fromFile(fd))
  def loadFileNodes(name: String): Seq[Node] = loadNodes(fromFile(name))
  def loadNodes(is: InputStream): Seq[Node] = loadNodes(fromInputStream(is))
  def loadNodes(reader: Reader): Seq[Node] = loadNodes(fromReader(reader))
  def loadNodes(sysID: String): Seq[Node] = loadNodes(fromSysId(sysID))
  def loadNodes(url: URL): Seq[Node] = loadNodes(fromInputStream(url.openStream()))
  def loadStringNodes(string: String): Seq[Node] = loadNodes(fromString(string))
}
