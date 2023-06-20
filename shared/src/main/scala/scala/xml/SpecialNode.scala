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
 * `SpecialNode` is a special XML node which represents either text
 *  `(PCDATA)`, a comment, a `PI`, or an entity ref.
 *
 *  @author Burak Emir
 */
abstract class SpecialNode extends Node {

  /** always empty */
  final override def attributes: Null.type = Null

  /** always Node.EmptyNamespace - TODO not really: Node.EmptyNamespace is "", but this is null. */
  final override def namespace: scala.Null = null

  /** always empty */
  final override def child: ScalaVersionSpecificReturnTypes.SpecialNodeChild = Nil

  /** Append string representation to the given string buffer argument. */
  def buildString(sb: StringBuilder): StringBuilder
}
