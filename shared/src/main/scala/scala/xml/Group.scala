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
final case class Group(nodes: Seq[Node]) extends Node {
  override def theSeq: Seq[Node] = nodes

  override def canEqual(other: Any): Boolean = other match {
    case _: Group => true
    case _        => false
  }

  override def strict_==(other: Equality): Boolean = other match {
    case Group(xs) => nodes sameElements xs
    case _         => false
  }

  override protected def basisForHashCode: Seq[Node] = nodes

  /**
   * Since Group is very much a hack it throws an exception if you
   *  try to do anything with it.
   */
  private def fail(msg: String): Nothing = throw new UnsupportedOperationException("class Group does not support method '%s'" format msg)

  override def label: Nothing = fail("label")
  override def attributes: Nothing = fail("attributes")
  override def namespace: Nothing = fail("namespace")
  override def child: Nothing = fail("child")
  def buildString(sb: StringBuilder): Nothing = fail("toString(StringBuilder)")
}
