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
 * The class `Text` implements an XML node for text (PCDATA).
 *  It is used in both non-bound and bound XML representations.
 *
 *  @author Burak Emir
 *  @param data the text contained in this node, may not be null.
 */
// Note: used by the Scala compiler.
class Text(data: String) extends Atom[String](data) {

  /**
   * Returns text, with some characters escaped according to the XML
   *  specification.
   */
  override def buildString(sb: StringBuilder): StringBuilder =
    Utility.escape(data, sb)
}

/**
 * This singleton object contains the `apply`and `unapply` methods for
 *  convenient construction and deconstruction.
 *
 *  @author  Burak Emir
 */
// Note: used by the Scala compiler.
object Text {
  def apply(data: String): Text = new Text(data)
  def unapply(other: Any): Option[String] = other match {
    case x: Text => Some(x.data)
    case _       => None
  }
}
