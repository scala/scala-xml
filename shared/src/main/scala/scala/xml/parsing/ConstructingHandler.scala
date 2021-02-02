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

/** Implementation of MarkupHandler that constructs nodes.
  *
  *  @author  Burak Emir
  */
abstract class ConstructingHandler extends MarkupHandler {
  val preserveWS: Boolean

  def elem(
      pos: Int,
      pre: String,
      label: String,
      attrs: MetaData,
      pscope: NamespaceBinding,
      empty: Boolean,
      nodes: NodeSeq
  ): NodeSeq =
    Elem(pre, label, attrs, pscope, empty, nodes: _*)

  def procInstr(pos: Int, target: String, txt: String) =
    ProcInstr(target, txt)

  def comment(pos: Int, txt: String) = Comment(txt)
  def entityRef(pos: Int, n: String) = EntityRef(n)
  def text(pos: Int, txt: String) = Text(txt)
}
