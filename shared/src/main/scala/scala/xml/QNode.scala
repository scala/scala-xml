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
 * This object provides an extractor method to match a qualified node with
 *  its namespace URI
 *
 *  @author  Burak Emir
 */
object QNode {
  def unapplySeq(n: Node): Some[(String, String, MetaData, ScalaVersionSpecific.SeqOfNode)] =
    Some((n.scope.getURI(n.prefix), n.label, n.attributes, n.child))
}
