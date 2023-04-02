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

import org.xml.sax.{SAXNotRecognizedException, SAXNotSupportedException, XMLReader}
import javax.xml.parsers.SAXParserFactory
import parsing.{FactoryAdapter, NoBindingFactoryAdapter}
import java.io.{File, FileDescriptor, InputStream, Reader}
import java.net.URL

/**
 * Presents collection of XML loading methods which use the parser
 *  created by "def parser" or the reader created by "def reader".
 */
trait XMLLoader[T <: Node] {
  import scala.xml.Source._
  def adapter: FactoryAdapter = new NoBindingFactoryAdapter()

  private def setSafeDefaults(parserFactory: SAXParserFactory): Unit = {
    parserFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true)
    parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    parserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    parserFactory.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false)
    parserFactory.setXIncludeAware(false)
    parserFactory.setNamespaceAware(false)
  }

  private lazy val parserInstance: ThreadLocal[SAXParser] = new ThreadLocal[SAXParser] {
    override def initialValue: SAXParser = {
      val parserFactory: SAXParserFactory = SAXParserFactory.newInstance
      setSafeDefaults(parserFactory)
      parserFactory.newSAXParser
    }
  }

  /* Override this to use a different SAXParser. */
  def parser: SAXParser = parserInstance.get

  /* Override this to use a different XMLReader. */
  def reader: XMLReader = parser.getXMLReader

  /**
   * Loads XML from the given InputSource, using the supplied parser.
   * The methods available in scala.xml.XML use the XML parser in the JDK
   * (unless another parser is present on the classpath).
   */
  def loadXML(inputSource: InputSource, parser: SAXParser): T = loadXML(inputSource, parser.getXMLReader)

  def loadXMLNodes(inputSource: InputSource, parser: SAXParser): Seq[Node] = loadXMLNodes(inputSource, parser.getXMLReader)

  private def loadXML(inputSource: InputSource, reader: XMLReader): T = {
    val result: FactoryAdapter = parse(inputSource, reader)
    result.rootElem.asInstanceOf[T]
  }

  private def loadXMLNodes(inputSource: InputSource, reader: XMLReader): Seq[Node] = {
    val result: FactoryAdapter = parse(inputSource, reader)
    result.prolog ++ (result.rootElem :: result.epilogue)
  }

  private def parse(inputSource: InputSource, xmlReader: XMLReader): FactoryAdapter = {
    if (inputSource == null) throw new IllegalArgumentException("InputSource cannot be null")

    val result: FactoryAdapter = adapter

    xmlReader.setContentHandler(result)
    xmlReader.setDTDHandler(result)
    /* Do not overwrite pre-configured EntityResolver. */
    if (xmlReader.getEntityResolver == null) xmlReader.setEntityResolver(result)
    /* Do not overwrite pre-configured ErrorHandler. */
    if (xmlReader.getErrorHandler == null) xmlReader.setErrorHandler(result)

    try {
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", result)
    } catch {
      case _: SAXNotRecognizedException =>
      case _: SAXNotSupportedException =>
    }

    result.scopeStack = TopScope :: result.scopeStack
    xmlReader.parse(inputSource)
    result.scopeStack = result.scopeStack.tail

    result
  }

  /** Loads XML. */
  def load(inputSource: InputSource): T = loadXML(inputSource, reader)
  def loadFile(fileName: String): T = load(fromFile(fileName))
  def loadFile(file: File): T = load(fromFile(file))
  def load(url: URL): T = load(fromUrl(url))
  def load(sysId: String): T = load(fromSysId(sysId))
  def loadFile(fileDescriptor: FileDescriptor): T = load(fromFile(fileDescriptor))
  def load(inputStream: InputStream): T = load(fromInputStream(inputStream))
  def load(reader: Reader): T = load(fromReader(reader))
  def loadString(string: String): T = load(fromString(string))

  /** Load XML nodes, including comments and processing instructions that precede and follow the root element. */
  def loadNodes(inputSource: InputSource): Seq[Node] = loadXMLNodes(inputSource, reader)
  def loadFileNodes(fileName: String): Seq[Node] = loadNodes(fromFile(fileName))
  def loadFileNodes(file: File): Seq[Node] = loadNodes(fromFile(file))
  def loadNodes(url: URL): Seq[Node] = loadNodes(fromUrl(url))
  def loadNodes(sysId: String): Seq[Node] = loadNodes(fromSysId(sysId))
  def loadFileNodes(fileDescriptor: FileDescriptor): Seq[Node] = loadNodes(fromFile(fileDescriptor))
  def loadNodes(inputStream: InputStream): Seq[Node] = loadNodes(fromInputStream(inputStream))
  def loadNodes(reader: Reader): Seq[Node] = loadNodes(fromReader(reader))
  def loadStringNodes(string: String): Seq[Node] = loadNodes(fromString(string))
}
