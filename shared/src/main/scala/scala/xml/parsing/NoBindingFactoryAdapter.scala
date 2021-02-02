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

import scala.collection.Seq
import factory.NodeFactory

/** nobinding adaptor providing callbacks to parser to create elements.
  *   implements hash-consing
  */
class NoBindingFactoryAdapter extends FactoryAdapter with NodeFactory[Elem] {

  /** True.  Every XML node may contain text that the application needs */
  def nodeContainsText(label: String) = true

  /** From NodeFactory.  Constructs an instance of scala.xml.Elem -- TODO: deprecate as in Elem */
  protected def create(
      pre: String,
      label: String,
      attrs: MetaData,
      scope: NamespaceBinding,
      children: Seq[Node]
  ): Elem =
    Elem(pre, label, attrs, scope, children.isEmpty, children: _*)

  /** From FactoryAdapter.  Creates a node. never creates the same node twice, using hash-consing.
    *     TODO: deprecate as in Elem, or forward to create??
    */
  def createNode(
      pre: String,
      label: String,
      attrs: MetaData,
      scope: NamespaceBinding,
      children: List[Node]
  ): Elem =
    Elem(pre, label, attrs, scope, children.isEmpty, children: _*)

  /** Creates a text node. */
  def createText(text: String) = Text(text)

  /** Creates a processing instruction. */
  def createProcInstr(target: String, data: String) =
    makeProcInstr(target, data)
}
