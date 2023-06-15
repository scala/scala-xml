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
package transform

import scala.collection.Seq

/**
 * A class for XML transformations.
 *
 *  @author  Burak Emir
 */
abstract class BasicTransformer extends (Node => Node) {
  protected def unchanged(n: Node, ns: Seq[Node]): Boolean =
    ns.length == 1 && (ns.head == n)

  /**
   * Call transform(Node) for each node in ns, append results
   *  to NodeBuffer.
   */
  def transform(it: Iterator[Node], nb: NodeBuffer): Seq[Node] =
    it.foldLeft(nb)(_ ++= transform(_))

  /**
   * Call transform(Node) to each node in ns, yield ns if nothing changes,
   *  otherwise a new sequence of concatenated results.
   */
  def transform(ns: Seq[Node]): Seq[Node] = {
    val changed: Seq[Node] = ns.flatMap(transform)
    if (changed.length != ns.length || changed.zip(ns).exists(p => p._1 != p._2)) changed
    else ns
}

  def transform(n: Node): Seq[Node] = {
    if (n.doTransform) n match {
      case Group(xs) => Group(transform(xs)) // un-group the hack Group tag
      case _ =>
        val ch: Seq[Node] = n.child
        val nch: Seq[Node] = transform(ch)

        if (ch.eq(nch)) n
        else Elem(n.prefix, n.label, n.attributes, n.scope, nch.isEmpty, nch: _*)
    }
    else n
  }

  override def apply(n: Node): Node = {
    val seq: Seq[Node] = transform(n)
    if (seq.length > 1)
      throw new UnsupportedOperationException("transform must return single node for root")
    else seq.head
  }
}
