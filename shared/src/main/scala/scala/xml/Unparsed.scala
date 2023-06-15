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
 * An XML node for unparsed content. It will be output verbatim, all bets
 *  are off regarding wellformedness etc.
 *
 *  @author Burak Emir
 *  @param data content in this node, may not be null.
 */
class Unparsed(data: String) extends Atom[String](data) {

  /**
   * Returns text, with some characters escaped according to XML
   *  specification.
   */
  override def buildString(sb: StringBuilder): StringBuilder =
    sb.append(data)
}

/**
 * This singleton object contains the `apply`and `unapply` methods for
 *  convenient construction and deconstruction.
 *
 *  @author  Burak Emir
 */
object Unparsed {
  def apply(data: String): Unparsed = new Unparsed(data)
  def unapply(x: Unparsed): Some[String] = Some(x.data)
}
