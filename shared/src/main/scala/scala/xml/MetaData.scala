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

import Utility.sbToString
import scala.annotation.tailrec
import scala.collection.AbstractIterable
import scala.collection.Seq

object MetaData {
  /**
   * appends all attributes from new_tail to attribs, without attempting to
   * detect or remove duplicates. The method guarantees that all attributes
   * from attribs come before the attributes in new_tail, but does not
   * guarantee to preserve the relative order of attribs.
   *
   * Duplicates can be removed with `normalize`.
   */
  @tailrec
  def concatenate(attribs: MetaData, new_tail: MetaData): MetaData =
    if (attribs.isNull) new_tail
    else concatenate(attribs.next, attribs.copy(new_tail))

  /**
   * returns normalized MetaData, with all duplicates removed and namespace prefixes resolved to
   *  namespace URIs via the given scope.
   */
  def normalize(attribs: MetaData, scope: NamespaceBinding): MetaData = {
    def iterate(md: MetaData, normalized_attribs: MetaData, set: Set[String]): MetaData =
      if (md.isNull) {
        normalized_attribs
      } else if (md.value == null)
        iterate(md.next, normalized_attribs, set)
      else {
        val key: String = getUniversalKey(md, scope)
        if (set(key))
          iterate(md.next, normalized_attribs, set)
        else
          md.copy(iterate(md.next, normalized_attribs, set + key))
      }

    iterate(attribs, Null, Set())
  }

  /**
   * returns key if md is unprefixed, pre+key is md is prefixed
   */
  def getUniversalKey(attrib: MetaData, scope: NamespaceBinding): String = attrib match {
    case prefixed: PrefixedAttribute     => scope.getURI(prefixed.pre) + prefixed.key
    case unprefixed: UnprefixedAttribute => unprefixed.key
  }

  /**
   *  returns MetaData with attributes updated from given MetaData
   */
  def update(attribs: MetaData, scope: NamespaceBinding, updates: MetaData): MetaData =
    normalize(concatenate(updates, attribs), scope)

}

/**
 * This class represents an attribute and at the same time a linked list of
 *  attributes. Every instance of this class is either
 *  - an instance of `UnprefixedAttribute key,value` or
 *  - an instance of `PrefixedAttribute namespace_prefix,key,value` or
 *  - `Null`, the empty attribute list.
 *
 *  Namespace URIs are obtained by using the namespace scope of the element
 *  owning this attribute (see `getNamespace`).
 */
// Note: used by the Scala compiler.
abstract class MetaData
  extends AbstractIterable[MetaData]
  with Iterable[MetaData]
  with Equality
  with Serializable
  with ScalaVersionSpecificMetaData
{
  private[xml] def isNull: Boolean = this.eq(Null)

  /**
   * Updates this MetaData with the MetaData given as argument. All attributes that occur in updates
   *  are part of the resulting MetaData. If an attribute occurs in both this instance and
   *  updates, only the one in updates is part of the result (avoiding duplicates). For prefixed
   *  attributes, namespaces are resolved using the given scope, which defaults to TopScope.
   *
   *  @param updates MetaData with new and updated attributes
   *  @return a new MetaData instance that contains old, new and updated attributes
   */
  def append(updates: MetaData, scope: NamespaceBinding = TopScope): MetaData =
    MetaData.update(this, scope, updates)

  /**
   * Gets value of unqualified (unprefixed) attribute with given key, null if not found
   *
   * @param  key
   * @return value as Seq[Node] if key is found, null otherwise
   */
  def apply(key: String): ScalaVersionSpecific.SeqOfNode

  /**
   * convenience method, same as `apply(namespace, owner.scope, key)`.
   *
   *  @param namespace_uri namespace uri of key
   *  @param owner the element owning this attribute list
   *  @param key   the attribute key
   */
  final def apply(namespace_uri: String, owner: Node, key: String): ScalaVersionSpecific.SeqOfNode =
    apply(namespace_uri, owner.scope, key)

  /**
   * Gets value of prefixed attribute with given key and namespace, null if not found
   *
   * @param  namespace_uri namespace uri of key
   * @param  scp a namespace scp (usually of the element owning this attribute list)
   * @param  k   to be looked for
   * @return value as Seq[Node] if key is found, null otherwise
   */
  def apply(namespace_uri: String, scp: NamespaceBinding, k: String): ScalaVersionSpecific.SeqOfNode

  /**
   * returns a copy of this MetaData item with next field set to argument.
   */
  def copy(next: MetaData): MetaData

  /** if owner is the element of this metadata item, returns namespace */
  def getNamespace(owner: Node): String

  def hasNext: Boolean = Null != next

  def length: Int = length(0)

  def length(i: Int): Int = next.length(i + 1)

  def isPrefixed: Boolean

  override def canEqual(other: Any): Boolean = other match {
    case _: MetaData => true
    case _           => false
  }
  override def strict_==(other: Equality): Boolean = other match {
    case m: MetaData => this.asAttrMap == m.asAttrMap
    case _           => false
  }
  override protected def basisForHashCode: Seq[Any] = List(this.asAttrMap)

  /** filters this sequence of meta data */
  override def filter(f: MetaData => Boolean): MetaData =
    if (f(this)) copy(next.filter(f))
    else next.filter(f)

  def reverse: MetaData =
    foldLeft(Null: MetaData) { (x, xs) =>
        xs.copy(x)
    }

  /** returns key of this MetaData item */
  def key: String

  /** returns value of this MetaData item */
  def value: ScalaVersionSpecific.SeqOfNode

  /**
   * Returns a String containing "prefix:key" if the first key is
   *  prefixed, and "key" otherwise.
   */
  def prefixedKey: String = this match {
    case x: Attribute if x.isPrefixed => s"${x.pre}:$key"
    case _                            => key
  }

  /**
   * Returns a Map containing the attributes stored as key/value pairs.
   */
  def asAttrMap: Map[String, String] =
    iterator.map(x => (x.prefixedKey, NodeSeq.fromSeq(x.value).text)).toMap

  /** returns Null or the next MetaData item */
  def next: MetaData

  /**
   * Gets value of unqualified (unprefixed) attribute with given key, None if not found
   *
   * @param  key
   * @return value in Some(Seq[Node]) if key is found, None otherwise
   */
  final def get(key: String): Option[ScalaVersionSpecific.SeqOfNode] = Option(apply(key))

  /** same as get(uri, owner.scope, key) */
  final def get(uri: String, owner: Node, key: String): Option[ScalaVersionSpecific.SeqOfNode] =
    get(uri, owner.scope, key)

  /**
   * gets value of qualified (prefixed) attribute with given key.
   *
   * @param  uri namespace of key
   * @param  scope a namespace scp (usually of the element owning this attribute list)
   * @param  key to be looked fore
   * @return value as `Some[Seq[Node]]` if key is found, None otherwise
   */
  final def get(uri: String, scope: NamespaceBinding, key: String): Option[ScalaVersionSpecific.SeqOfNode] =
    Option(apply(uri, scope, key))

  protected def toString1: String = sbToString(toString1)

  // appends string representations of single attribute to StringBuilder
  protected def toString1(sb: StringBuilder): Unit

  override def toString: String = sbToString(buildString)

  def buildString(sb: StringBuilder): StringBuilder = {
    sb.append(' ')
    toString1(sb)
    next.buildString(sb)
  }

  /**
   */
  def wellformed(scope: NamespaceBinding): Boolean

  def remove(key: String): MetaData

  def remove(namespace: String, scope: NamespaceBinding, key: String): MetaData

  final def remove(namespace: String, owner: Node, key: String): MetaData =
    remove(namespace, owner.scope, key)
}
