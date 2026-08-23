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

import java.io.InputStream
import scala.util.matching.Regex
import xml.Nullables._

/**
 * `EncodingHeuristics` reads from a stream
 * (which should be buffered) and attempts to guess
 * what the encoding of the text in the stream is.
 * If it fails to determine the type of the encoding,
 * it returns the default UTF-8.
 *
 * @author Burak Emir
 * @author Paul Phillips
 */
object EncodingHeuristics {
  object EncodingNames {
    // UCS-4 isn't yet implemented in java releases anyway...
    val bigUCS4: String = "UCS-4"
    val littleUCS4: String = "UCS-4"
    val unusualUCS4: String = "UCS-4"
    val bigUTF16: String = "UTF-16BE"
    val littleUTF16: String = "UTF-16LE"
    val utf8: String = "UTF-8"
    val default: String = utf8
  }
  import EncodingNames._

  /**
   * This utility method attempts to determine the XML character encoding
   * by examining the input stream, as specified at
   * [[http://www.w3.org/TR/xml/#sec-guessing w3]].
   *
   * @param    in   `InputStream` to read from.
   * @throws java.io.IOException if the stream cannot be reset
   * @return         the name of the encoding.
   */
  def readEncodingFromStream(in: InputStream): String = {
    var ret: Nullable[String] = null
    val bytesToRead: Int = 1024 // enough to read most XML encoding declarations
    def resetAndRet: Nullable[String] = { in.reset(); ret }

    // This may fail if there are a lot of space characters before the end
    // of the encoding declaration
    in mark bytesToRead
    val bytes: (Int, Int, Int, Int) = (in.read, in.read, in.read, in.read)

    // first look for byte order mark
    ret = bytes match {
      case (0x00, 0x00, 0xFE, 0xFF) => bigUCS4
      case (0xFF, 0xFE, 0x00, 0x00) => littleUCS4
      case (0x00, 0x00, 0xFF, 0xFE) => unusualUCS4
      case (0xFE, 0xFF, 0x00, 0x00) => unusualUCS4
      case (0xFE, 0xFF, _, _)       => bigUTF16
      case (0xFF, 0xFE, _, _)       => littleUTF16
      case (0xEF, 0xBB, 0xBF, _)    => utf8
      case _                        => null
    }
    if (ret != null)
      return resetAndRet.nn

    def readASCIIEncoding: String = {
      val data: Array[Byte] = new Array[Byte](bytesToRead - 4)
      val length: Int = in.read(data, 0, bytesToRead - 4)

      // Use Latin-1 (ISO-8859-1) because all byte sequences are legal.
      val declaration: String = new String(data, 0, length, "ISO-8859-1")
      val regexp: Regex = """(?m).*?encoding\s*=\s*["'](.+?)['"]""".r
      regexp.findFirstMatchIn(declaration) match {
        case None     => default
        case Some(md) => md.subgroups(0)
      }
    }

    // no byte order mark present; first character must be '<' or whitespace
    ret = bytes match {
      case (0x00, 0x00, 0x00, '<')  => bigUCS4
      case ('<', 0x00, 0x00, 0x00)  => littleUCS4
      case (0x00, 0x00, '<', 0x00)  => unusualUCS4
      case (0x00, '<', 0x00, 0x00)  => unusualUCS4
      case (0x00, '<', 0x00, '?')   => bigUTF16 // XXX must read encoding
      case ('<', 0x00, '?', 0x00)   => littleUTF16 // XXX must read encoding
      case ('<', '?', 'x', 'm')     => readASCIIEncoding
      case (0x4C, 0x6F, 0xA7, 0x94) => utf8 // XXX EBCDIC
      case _                        => utf8 // no XML or text declaration present
    }
    resetAndRet.nn
  }
}
