/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2020, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    (c) 2011-2020, Lightbend, Inc.       **
** /____/\___/_/ |_/____/_/ | |    http://scala-lang.org/               **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package include.sax

import java.io.InputStream

/** `EncodingHeuristics` reads from a stream
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
    val bigUCS4 = "UCS-4"
    val littleUCS4 = "UCS-4"
    val unusualUCS4 = "UCS-4"
    val bigUTF16 = "UTF-16BE"
    val littleUTF16 = "UTF-16LE"
    val utf8 = "UTF-8"
    val default = utf8
  }
  import EncodingNames._

  /** This utility method attempts to determine the XML character encoding
    * by examining the input stream, as specified at
    * [[http://www.w3.org/TR/xml/#sec-guessing w3]].
    *
    * @param    in   `InputStream` to read from.
    * @throws java.io.IOException if the stream cannot be reset
    * @return         the name of the encoding.
    */
  def readEncodingFromStream(in: InputStream): String = {
    var ret: String = null
    val bytesToRead = 1024 // enough to read most XML encoding declarations
    def resetAndRet = { in.reset; ret }

    // This may fail if there are a lot of space characters before the end
    // of the encoding declaration
    in mark bytesToRead
    val bytes = (in.read, in.read, in.read, in.read)

    // first look for byte order mark
    ret = bytes match {
      case (0x00, 0x00, 0xfe, 0xff) => bigUCS4
      case (0xff, 0xfe, 0x00, 0x00) => littleUCS4
      case (0x00, 0x00, 0xff, 0xfe) => unusualUCS4
      case (0xfe, 0xff, 0x00, 0x00) => unusualUCS4
      case (0xfe, 0xff, _, _)       => bigUTF16
      case (0xff, 0xfe, _, _)       => littleUTF16
      case (0xef, 0xbb, 0xbf, _)    => utf8
      case _                        => null
    }
    if (ret != null)
      return resetAndRet

    def readASCIIEncoding: String = {
      val data = new Array[Byte](bytesToRead - 4)
      val length = in.read(data, 0, bytesToRead - 4)

      // Use Latin-1 (ISO-8859-1) because all byte sequences are legal.
      val declaration = new String(data, 0, length, "ISO-8859-1")
      val regexp = """(?m).*?encoding\s*=\s*["'](.+?)['"]""".r
      (regexp findFirstMatchIn declaration) match {
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
      case (0x4c, 0x6f, 0xa7, 0x94) => utf8 // XXX EBCDIC
      case _                        => utf8 // no XML or text declaration present
    }
    resetAndRet
  }
}
