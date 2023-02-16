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
 * This singleton object contains the `unapplySeq` method for
 *  convenient deconstruction.
 *
 *  @author  Burak Emir
 */
object Node {
  /** the constant empty attribute sequence */
  final def NoAttributes: MetaData = Null

  /** the empty namespace */
  val EmptyNamespace: String = ""

  def unapplySeq(n: Node): Some[(String, MetaData, Seq[Node])] = Some((n.label, n.attributes, n.child.toSeq))
}

/**
 * An abstract class representing XML with nodes of a labeled tree.
 * This class contains an implementation of a subset of XPath for navigation.
 *
 *  - [[scala.xml.Comment]] — XML comment
 *  - [[scala.xml.Elem]] — XML element
 *  - [[scala.xml.EntityRef]] — XML entity
 *  - [[scala.xml.PCData]] — Character data section (CDATA)
 *  - [[scala.xml.ProcInstr]] — Processing instruction (PI)
 *  - [[scala.xml.Text]] — Stand-alone parsed character data
 *
 * @author  Burak Emir and others
 */
abstract class Node extends NodeSeq {

  /** prefix of this node */
  def prefix: String = null

  /** label of this node. I.e. "foo" for &lt;foo/&gt;) */
  def label: String

  /**
   * used internally. Atom/Molecule = -1 PI = -2 Comment = -3 EntityRef = -5
   */
  def isAtom: Boolean = this.isInstanceOf[Atom[_]]

  /** The logic formerly found in typeTag$, as best I could infer it. */
  def doCollectNamespaces: Boolean = true // if (tag >= 0) DO collect namespaces
  def doTransform: Boolean = true // if (tag < 0) DO NOT transform

  /**
   *  method returning the namespace bindings of this node. by default, this
   *  is TopScope, which means there are no namespace bindings except the
   *  predefined one for "xml".
   */
  def scope: NamespaceBinding = TopScope

  /**
   *  convenience, same as `getNamespace(this.prefix)`
   */
  def namespace: String = getNamespace(this.prefix)

  /**
   * Convenience method, same as `scope.getURI(pre)` but additionally
   * checks if scope is `'''null'''`.
   *
   * @param pre the prefix whose namespace name we would like to obtain
   * @return    the namespace if `scope != null` and prefix was
   *            found, else `null`
   */
  def getNamespace(pre: String): String = if (scope eq null) null else scope.getURI(pre)

  /**
   * Convenience method, looks up an unprefixed attribute in attributes of this node.
   * Same as `attributes.getValue(key)`
   *
   * @param  key of queried attribute.
   * @return value of `UnprefixedAttribute` with given key
   *         in attributes, if it exists, otherwise `null`.
   */
  final def attribute(key: String): Option[Seq[Node]] = attributes.get(key)

  /**
   * Convenience method, looks up a prefixed attribute in attributes of this node.
   * Same as `attributes.getValue(uri, this, key)`-
   *
   * @param  uri namespace of queried attribute (may not be null).
   * @param  key of queried attribute.
   * @return value of `PrefixedAttribute` with given namespace
   *         and given key, otherwise `'''null'''`.
   */
  final def attribute(uri: String, key: String): Option[Seq[Node]] =
    attributes.get(uri, this, key)

  /**
   * Returns attribute meaning all attributes of this node, prefixed and
   * unprefixed, in no particular order. In class `Node`, this
   * defaults to `Null` (the empty attribute list).
   *
   * @return all attributes of this node
   */
  def attributes: MetaData = Null

  /**
   * Returns child axis i.e. all children of this node.
   *
   * @return all children of this node
   */
  def child: Seq[Node]

  /**
   * Children which do not stringify to "" (needed for equality)
   */
  def nonEmptyChildren: Seq[Node] = child filterNot (_.toString == "")

  /**
   * Descendant axis (all descendants of this node, not including node itself)
   * includes all text nodes, element nodes, comments and processing instructions.
   */
  def descendant: List[Node] =
    child.toList.flatMap { x => x :: x.descendant }

  /**
   * Descendant axis (all descendants of this node, including this node)
   * includes all text nodes, element nodes, comments and processing instructions.
   */
  def descendant_or_self: List[Node] = this :: descendant

  override def canEqual(other: Any): Boolean = other match {
    case _: Group => false
    case _: Node  => true
    case _        => false
  }

  override protected def basisForHashCode: Seq[Any] =
    prefix :: label :: attributes :: nonEmptyChildren.toList

  override def strict_==(other: Equality): Boolean = other match {
    case _: Group => false
    case x: Node =>
      (prefix == x.prefix) &&
        (label == x.label) &&
        (attributes == x.attributes) &&
        // (scope == x.scope)               // note - original code didn't compare scopes so I left it as is.
        (nonEmptyChildren sameElements x.nonEmptyChildren)
    case _ =>
      false
  }

  // implementations of NodeSeq methods

  /**
   *  returns a sequence consisting of only this node
   */
  override def theSeq: Seq[Node] = this :: Nil

  /**
   * String representation of this node
   *
   * @param stripComments if true, strips comment nodes from result
   */
  def buildString(stripComments: Boolean): String =
    Utility.serialize(this, stripComments = stripComments).toString

  /**
   * Same as `toString('''false''')`.
   */
  override def toString: String = buildString(stripComments = false)

  /**
   * Appends qualified name of this node to `StringBuilder`.
   */
  def nameToString(sb: StringBuilder): StringBuilder = {
    if (null != prefix) {
      sb append prefix
      sb append ':'
    }
    sb append label
  }

  /**
   * Returns a type symbol (e.g. DTD, XSD), default `'''null'''`.
   */
  def xmlType: TypeSymbol = null

  /**
   * Returns a text representation of this node. Note that this is not equivalent to
   * the XPath node-test called text(), it is rather an implementation of the
   * XPath function string()
   *  Martin to Burak: to do: if you make this method abstract, the compiler will now
   *  complain if there's no implementation in a subclass. Is this what we want? Note that
   *  this would break doc/DocGenator and doc/ModelToXML, with an error message like:
   * {{{
   * doc\DocGenerator.scala:1219: error: object creation impossible, since there is a deferred declaration of method text in class Node of type => String which is not implemented in a subclass
   * new SpecialNode {
   * ^
   * }}}
   */
  override def text: String = super.text
}
