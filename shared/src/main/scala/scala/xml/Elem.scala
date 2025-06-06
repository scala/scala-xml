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
 * This singleton object contains the `apply` and `unapplySeq` methods for
 *  convenient construction and deconstruction. It is possible to deconstruct
 *  any `Node` instance (that is not a `SpecialNode` or a `Group`) using the
 *  syntax `case Elem(prefix, label, attribs, scope, child @ _*) => ...`
 */
// Note: used by the Scala compiler.
object Elem {

  def apply(prefix: String, label: String, attributes: MetaData, scope: NamespaceBinding, minimizeEmpty: Boolean, child: Node*): Elem =
    new Elem(prefix, label, attributes, scope, minimizeEmpty, child: _*)

  def unapplySeq(n: Node): Option[(String, String, MetaData, NamespaceBinding, ScalaVersionSpecific.SeqOfNode)] =
    n match {
      case _: SpecialNode | _: Group => None
      case _                         => Some((n.prefix, n.label, n.attributes, n.scope, n.child))
    }
}

/**
 * An immutable data object representing an XML element.
 *
 * Child elements can be other [[Elem]]s or any one of the other [[Node]] types.
 *
 * XML attributes are implemented with the [[scala.xml.MetaData]] base
 * class.
 *
 * Optional XML namespace scope is represented by
 * [[scala.xml.NamespaceBinding]].
 *
 *  @param prefix        namespace prefix (may be null, but not the empty string)
 *  @param label         the element name
 *  @param attributes1   the attribute map
 *  @param scope         the scope containing the namespace bindings
 *  @param minimizeEmpty `true` if this element should be serialized as minimized (i.e. "&lt;el/&gt;") when
 *                       empty; `false` if it should be written out in long form.
 *  @param child         the children of this node
 */
// Note: used by the Scala compiler.
class Elem(
  override val prefix: String,
  override val label: String,
  attributes1: MetaData,
  override val scope: NamespaceBinding,
  val minimizeEmpty: Boolean,
  override val child: Node*
) extends Node with Serializable {

  final override def doCollectNamespaces: Boolean = true
  final override def doTransform: Boolean = true

  override val attributes: MetaData = MetaData.normalize(attributes1, scope)

  if (prefix == "")
    throw new IllegalArgumentException("prefix of zero length, use null instead")

  if (scope == null)
    throw new IllegalArgumentException("scope is null, use scala.xml.TopScope for empty scope")

  //@todo: copy the children,
  //  setting namespace scope if necessary
  //  cleaning adjacent text nodes if necessary

  override protected def basisForHashCode: Seq[Any] =
    prefix :: label :: attributes :: child.toList

  /**
   * Returns a new element with updated attributes, resolving namespace uris
   *  from this element's scope. See MetaData.update for details.
   *
   *  @param  updates MetaData with new and updated attributes
   *  @return a new symbol with updated attributes
   */
  final def %(updates: MetaData): Elem =
    copy(attributes = MetaData.update(attributes, scope, updates))

  /**
   * Returns a copy of this element with any supplied arguments replacing
   *  this element's value for that field.
   *
   *  @return a new symbol with updated attributes
   */
  def copy(
    prefix: String = this.prefix,
    label: String = this.label,
    attributes: MetaData = this.attributes,
    scope: NamespaceBinding = this.scope,
    minimizeEmpty: Boolean = this.minimizeEmpty,
    child: Seq[Node] = this.child
  ): Elem = Elem(prefix, label, attributes, scope, minimizeEmpty, child.toSeq: _*)

  /**
   * Returns concatenation of `text(n)` for each child `n`.
   */
  override def text: String = child.map(_.text).mkString
}
