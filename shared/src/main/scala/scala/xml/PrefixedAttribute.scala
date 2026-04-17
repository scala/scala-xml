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

import xml.Nullables._
import scala.collection.Seq

/**
 * prefixed attributes always have a non-null namespace.
 *
 *  @param pre
 *  @param key
 *  @param _value the attribute value
 *  @param next1
 */
// Note: used by the Scala compiler.
class PrefixedAttribute(
  override val pre: String,
  override val key: String,
  _value: Nullable[Seq[Node]],
  val next1: MetaData
)
  extends Attribute
{
  override val value: Nullable[ScalaVersionSpecific.SeqOfNode] = if (_value == null) null else _value match {
    case ns: ScalaVersionSpecific.SeqOfNode => ns
    case _ => _value.toVector
  }

  override val next: MetaData = if (value != null) next1 else next1.remove(key)

  /** same as this(pre, key, Text(value), next), or no attribute if value is null */
  def this(pre: String, key: String, value: Nullable[String], next: MetaData) =
    this(pre, key, if (value != null) Text(value) else null: Nullable[NodeSeq], next)

  /** same as this(pre, key, value.get, next), or no attribute if value is None */
  def this(pre: String, key: String, value: Option[Seq[Node]], next: MetaData) =
    this(pre, key, value.orNull, next)

  /**
   * Returns a copy of this unprefixed attribute with the given
   *  next field.
   */
  override def copy(next: MetaData): PrefixedAttribute =
    new PrefixedAttribute(pre, key, value, next)

  override def getNamespace(owner: Node): Nullable[String] =
    owner.getNamespace(pre)

  /** forwards the call to next (because caller looks for unprefixed attribute */
  override def apply(key: String): Nullable[ScalaVersionSpecific.SeqOfNode] = next(key)

  /**
   * gets attribute value of qualified (prefixed) attribute with given key
   */
  override def apply(namespace: Nullable[String], scope: NamespaceBinding, key: Nullable[String]): Nullable[ScalaVersionSpecific.SeqOfNode] =
    if (key == this.key && scope.getURI(pre) == namespace)
      value
    else
      next(namespace, scope, key)
}

object PrefixedAttribute {
  def unapply(x: PrefixedAttribute): Some[(String, String, Nullable[Seq[Node]], MetaData)] = Some((x.pre, x.key, x.value, x.next))
}
