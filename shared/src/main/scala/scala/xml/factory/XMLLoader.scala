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

import org.xml.sax.XMLReader
import scala.xml.Source
import javax.xml.parsers.SAXParserFactory
import java.io.{File, FileDescriptor, InputStream, Reader}
import java.net.URL

/**
 * Presents collection of XML loading methods which use the parser
 *  created by "def parser" or the reader created by "def reader".
 */
trait XMLLoader[T <: Node] {
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
   * Loads XML from the given InputSource, using the supplied parser or reader.
   * The methods available in scala.xml.XML use the XML parser in the JDK
   * (unless another parser is present on the classpath).
   */

  // TODO remove
  def loadXML(inputSource: InputSource, parser: SAXParser): T = getDocElem(adapter.loadDocument(inputSource, parser.getXMLReader))
  def loadXMLNodes(inputSource: InputSource, parser: SAXParser): Seq[Node] = adapter.loadDocument(inputSource, parser.getXMLReader).children.toSeq
  def adapter: parsing.FactoryAdapter = new parsing.NoBindingFactoryAdapter()

  /** Loads XML Document. */
  def loadDocument(inputSource: InputSource): Document = adapter.loadDocument(inputSource, reader)
  def loadFileDocument(fileName: String): Document = loadDocument(Source.fromFile(fileName))
  def loadFileDocument(file: File): Document = loadDocument(Source.fromFile(file))
  def loadDocument(url: URL): Document = loadDocument(Source.fromUrl(url))
  def loadDocument(sysId: String): Document = loadDocument(Source.fromSysId(sysId))
  def loadFileDocument(fileDescriptor: FileDescriptor): Document = loadDocument(Source.fromFile(fileDescriptor))
  def loadDocument(inputStream: InputStream): Document = loadDocument(Source.fromInputStream(inputStream))
  def loadDocument(reader: Reader): Document = loadDocument(Source.fromReader(reader))
  def loadStringDocument(string: String): Document = loadDocument(Source.fromString(string))

  /** Loads XML element. */
  private def getDocElem(document: Document): T = document.docElem.asInstanceOf[T]
  def load(inputSource: InputSource): T = getDocElem(loadDocument(inputSource))
  def loadFile(fileName: String): T = getDocElem(loadFileDocument(fileName))
  def loadFile(file: File): T = getDocElem(loadFileDocument(file))
  def load(url: URL): T = getDocElem(loadDocument(url))
  def load(sysId: String): T = getDocElem(loadDocument(sysId))
  def loadFile(fileDescriptor: FileDescriptor): T = getDocElem(loadFileDocument(fileDescriptor))
  def load(inputStream: InputStream): T = getDocElem(loadDocument(inputStream))
  def load(reader: Reader): T = getDocElem(loadDocument(reader))
  def loadString(string: String): T = getDocElem(loadStringDocument(string))

  /** Load XML nodes, including comments and processing instructions that precede and follow the root element. */
  def loadNodes(inputSource: InputSource): Seq[Node] = loadDocument(inputSource).children.toSeq
  def loadFileNodes(fileName: String): Seq[Node] = loadFileDocument(fileName).children.toSeq
  def loadFileNodes(file: File): Seq[Node] = loadFileDocument(file).children.toSeq
  def loadNodes(url: URL): Seq[Node] = loadDocument(url).children.toSeq
  def loadNodes(sysId: String): Seq[Node] = loadDocument(sysId).children.toSeq
  def loadFileNodes(fileDescriptor: FileDescriptor): Seq[Node] = loadFileDocument(fileDescriptor).children.toSeq
  def loadNodes(inputStream: InputStream): Seq[Node] = loadDocument(inputStream).children.toSeq
  def loadNodes(reader: Reader): Seq[Node] = loadDocument(reader).children.toSeq
  def loadStringNodes(string: String): Seq[Node] = loadStringDocument(string).children.toSeq
}
