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

/**
 * This class (which is not used by all XML parsers, but always used by the
 *  XHTML one) represents parseable character data, which appeared as CDATA
 *  sections in the input and is to be preserved as CDATA section in the output.
 *
 *  @author  Burak Emir
 */
// Note: used by the Scala compiler (before Scala 3).
class PCData(data: String) extends Atom[String](data) {

  /**
   * Returns text, with some characters escaped according to the XML
   *  specification.
   *
   *  @param  sb the input string buffer associated to some XML element
   *  @return the input string buffer with the formatted CDATA section
   */
  override def buildString(sb: StringBuilder): StringBuilder = {
    val dataStr: String = data.replaceAll("]]>", "]]]]><![CDATA[>")
    sb.append(s"<![CDATA[$dataStr]]>")
  }
}

/**
 * This singleton object contains the `apply`and `unapply` methods for
 *  convenient construction and deconstruction.
 *
 *  @author  Burak Emir
 */
// Note: used by the Scala compiler (before Scala 3).
object PCData {
  def apply(data: String): PCData = new PCData(data)
  def unapply(other: Any): Option[String] = other match {
    case x: PCData => Some(x.data)
    case _         => None
  }
}

