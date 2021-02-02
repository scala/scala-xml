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
package parsing

/** Default implementation of markup handler always returns `NodeSeq.Empty` */
abstract class DefaultMarkupHandler extends MarkupHandler {

  def elem(
      pos: Int,
      pre: String,
      label: String,
      attrs: MetaData,
      scope: NamespaceBinding,
      empty: Boolean,
      args: NodeSeq
  ) = NodeSeq.Empty

  def procInstr(pos: Int, target: String, txt: String) = NodeSeq.Empty

  def comment(pos: Int, comment: String): NodeSeq = NodeSeq.Empty

  def entityRef(pos: Int, n: String) = NodeSeq.Empty

  def text(pos: Int, txt: String) = NodeSeq.Empty

}
