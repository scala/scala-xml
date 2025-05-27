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
 * A hack to group XML nodes in one node for output.
 *
 *  @author  Burak Emir
 */
// Note: used by the Scala compiler.
final case class Group(nodes: Seq[Node]) extends Node {
  // Ideally, the `immutable.Seq` would be stored as a field.
  // But evolving the case class and remaining binary compatible is very difficult
  // Since `Group` is used rarely, call `toSeq` on the field.
  // In practice, it should not matter - the `nodes` field anyway contains an `immutable.Seq`.
  override def theSeq: ScalaVersionSpecific.SeqOfNode = nodes.toSeq

  override def canEqual(other: Any): Boolean = other match {
    case _: Group => true
    case _        => false
  }

  override def strict_==(other: Equality): Boolean = other match {
    case Group(xs) => nodes.sameElements(xs)
    case _         => false
  }

  override protected def basisForHashCode: Seq[Node] = nodes

  /**
   * Since Group is very much a hack it throws an exception if you
   *  try to do anything with it.
   */
  private def fail(msg: String): Nothing = throw new UnsupportedOperationException(s"class Group does not support method '$msg'")

  override def label: Nothing = fail("label")
  override def attributes: Nothing = fail("attributes")
  override def namespace: Nothing = fail("namespace")
  override def child: ScalaVersionSpecificReturnTypes.GroupChild = fail("child")
  def buildString(sb: StringBuilder): Nothing = fail("toString(StringBuilder)")
}
