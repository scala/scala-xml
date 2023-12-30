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

import scala.collection.Seq

/**
 * The class `Atom` provides an XML node for text (`PCDATA`).
 *  It is used in both non-bound and bound XML representations.
 *
 *  @author Burak Emir
 *  @param data the text contained in this node, may not be `'''null'''`.
 */
class Atom[+A](val data: A) extends SpecialNode with Serializable {
  if (data == null)
    throw new IllegalArgumentException(s"cannot construct ${getClass.getSimpleName} with null")

  override protected def basisForHashCode: Seq[Any] = Seq(data)

  override def strict_==(other: Equality): Boolean = other match {
    case x: Atom[?] => data == x.data
    case _          => false
  }

  override def canEqual(other: Any): Boolean = other match {
    case _: Atom[?] => true
    case _          => false
  }

  final override def doCollectNamespaces: Boolean = false
  final override def doTransform: Boolean = false

  override def label: String = "#PCDATA"

  /**
   * Returns text, with some characters escaped according to the XML
   *  specification.
   */
  override def buildString(sb: StringBuilder): StringBuilder =
    Utility.escape(data.toString, sb)

  override def text: String = data.toString
}
