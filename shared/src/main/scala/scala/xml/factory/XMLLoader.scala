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

import org.xml.sax.SAXNotRecognizedException
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

  private lazy val parserInstance = new ThreadLocal[SAXParser] {
    override def initialValue: SAXParser = {
      val parser = SAXParserFactory.newInstance()
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

  /**
   * Loads XML from the given InputSource, using the supplied parser.
   *  The methods available in scala.xml.XML use the XML parser in the JDK.
   */
  def loadXML(source: InputSource, parser: SAXParser): T = {
    val result: FactoryAdapter = parse(source, parser)
    result.rootElem.asInstanceOf[T]
  }

  def loadXMLNodes(source: InputSource, parser: SAXParser): Seq[Node] = {
    val result: FactoryAdapter = parse(source, parser)
    result.prolog ++ (result.rootElem :: result.epilogue)
  }

  private def parse(source: InputSource, parser: SAXParser): FactoryAdapter = {
    val result: FactoryAdapter = adapter

    try {
      parser.setProperty("http://xml.org/sax/properties/lexical-handler", result)
    } catch {
      case _: SAXNotRecognizedException =>
    }

    result.scopeStack = TopScope :: result.scopeStack
    parser.parse(source, result)
    result.scopeStack = result.scopeStack.tail

    result
  }

  /** Loads XML from the given file, file descriptor, or filename. */
  def loadFile(file: File): T = loadXML(fromFile(file), parser)
  def loadFile(fd: FileDescriptor): T = loadXML(fromFile(fd), parser)
  def loadFile(name: String): T = loadXML(fromFile(name), parser)

  /** loads XML from given InputStream, Reader, sysID, InputSource, or URL. */
  def load(is: InputStream): T = loadXML(fromInputStream(is), parser)
  def load(reader: Reader): T = loadXML(fromReader(reader), parser)
  def load(sysID: String): T = loadXML(fromSysId(sysID), parser)
  def load(source: InputSource): T = loadXML(source, parser)
  def load(url: URL): T = loadXML(fromInputStream(url.openStream()), parser)

  /** Loads XML from the given String. */
  def loadString(string: String): T = loadXML(fromString(string), parser)

  /** Load XML nodes, including comments and processing instructions that precede and follow the root element. */
  def loadFileNodes(file: File): Seq[Node] = loadXMLNodes(fromFile(file), parser)
  def loadFileNodes(fd: FileDescriptor): Seq[Node] = loadXMLNodes(fromFile(fd), parser)
  def loadFileNodes(name: String): Seq[Node] = loadXMLNodes(fromFile(name), parser)
  def loadNodes(is: InputStream): Seq[Node] = loadXMLNodes(fromInputStream(is), parser)
  def loadNodes(reader: Reader): Seq[Node] = loadXMLNodes(fromReader(reader), parser)
  def loadNodes(sysID: String): Seq[Node] = loadXMLNodes(fromSysId(sysID), parser)
  def loadNodes(source: InputSource): Seq[Node] = loadXMLNodes(source, parser)
  def loadNodes(url: URL): Seq[Node] = loadXMLNodes(fromInputStream(url.openStream()), parser)
  def loadStringNodes(string: String): Seq[Node] = loadXMLNodes(fromString(string), parser)
}
