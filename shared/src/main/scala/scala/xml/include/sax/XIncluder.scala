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

import xml.Nullables._
import org.xml.sax.{ ContentHandler, Locator, Attributes }
import org.xml.sax.ext.LexicalHandler
import java.io.{ OutputStream, OutputStreamWriter, IOException }

/**
 * XIncluder is a SAX `ContentHandler` that writes its XML document onto
 * an output stream after resolving all `xinclude:include` elements.
 *
 * Based on Eliotte Rusty Harold's SAXXIncluder.
 */
class XIncluder(outs: OutputStream, encoding: String) extends ContentHandler with LexicalHandler {

  var out: OutputStreamWriter = new OutputStreamWriter(outs, encoding)

  override def setDocumentLocator(locator: Locator): Unit = ()

  override def startDocument(): Unit = {
    try {
      out.write(s"<?xml version='1.0' encoding='$encoding'?>\r\n")
    } catch {
      case e: IOException =>
        throw new SAXException("Write failed", e)
    }
  }

  override def endDocument(): Unit = {
    try {
      out.flush()
    } catch {
      case e: IOException =>
        throw new SAXException("Flush failed", e)
    }
  }

  override def startPrefixMapping(prefix: String, uri: String): Unit = ()

  override def endPrefixMapping(prefix: String): Unit = ()

  override def startElement(namespaceURI: String, localName: String, qualifiedName: String, atts: Attributes): Unit = {
    try {
      out.write(s"<$qualifiedName")
      var i: Int = 0
      while (i < atts.getLength) {
        val value: String = atts.getValue(i)
        // @todo Need to use character references if the encoding
        // can't support the character
        val valueStr: String = scala.xml.Utility.escape(value)
        out.write(s" ${atts.getQName(i)}='$valueStr'")
        i += 1
      }
      out.write(">")
    } catch {
      case e: IOException =>
        throw new SAXException("Write failed", e)
    }
  }

  override def endElement(namespaceURI: String, localName: String, qualifiedName: String): Unit = {
    try {
      out.write(s"</$qualifiedName>")
    } catch {
      case e: IOException =>
        throw new SAXException("Write failed", e)
    }
  }

  // need to escape characters that are not in the given
  // encoding using character references????
  override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
    try {
      var i: Int = 0
      while (i < length) {
        val c: Char = ch(start + i)
        if (c == '&') out.write("&amp;")
        else if (c == '<') out.write("&lt;")
        // This next fix is normally not necessary.
        // However, it is required if text contains ]]>
        // (The end CDATA section delimiter)
        else if (c == '>') out.write("&gt;")
        else out.write(c.toInt)
        i += 1
      }
    } catch {
      case e: IOException =>
        throw new SAXException("Write failed", e)
    }
  }

  override def ignorableWhitespace(ch: Array[Char], start: Int, length: Int): Unit = {
    this.characters(ch, start, length)
  }

  // do I need to escape text in PI????
  override def processingInstruction(target: String, data: String): Unit = {
    try {
      out.write(s"<?$target $data?>")
    } catch {
      case e: IOException =>
        throw new SAXException("Write failed", e)
    }
  }

  override def skippedEntity(name: String): Unit = {
    try {
      out.write(s"&$name;")
    } catch {
      case e: IOException =>
        throw new SAXException("Write failed", e)
    }
  }

  // LexicalHandler methods
  private var inDTD: Boolean = false
  private var entities = List.empty[String]

  override def startDTD(name: String, publicID: String, systemID: String): Unit = {
    inDTD = true
    // if this is the source document, output a DOCTYPE declaration
    if (entities.isEmpty) {
      var id: String = ""
      if (publicID != null) id = s""" PUBLIC "$publicID" "$systemID""""
      else if (systemID != null) id = s""" SYSTEM "$systemID""""
      try {
        out.write(s"<!DOCTYPE $name$id>\r\n")
      } catch {
        case e: IOException =>
          throw new SAXException("Error while writing DOCTYPE", e)
      }
    }
  }
  override def endDTD(): Unit = ()

  override def startEntity(name: String): Unit = {
    entities = name :: entities
  }

  override def endEntity(name: String): Unit = {
    entities = entities.tail
  }

  override def startCDATA(): Unit = ()
  override def endCDATA(): Unit = ()

  // Just need this reference so we can ask if a comment is
  // inside an include element or not
  private var filter: Nullable[XIncludeFilter] = _

  def setFilter(filter: XIncludeFilter): Unit = {
    this.filter = filter
  }

  override def comment(ch: Array[Char], start: Int, length: Int): Unit = {
    if (!inDTD && !filter.nn.insideIncludeElement) {
      try {
        out.write("<!--")
        out.write(ch, start, length)
        out.write("-->")
      } catch {
        case e: IOException =>
          throw new SAXException("Write failed", e)
      }
    }
  }
}
