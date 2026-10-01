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
 * This singleton object contains the `apply` and `unapply` methods for
 *  convenient construction and deconstruction.
 *
 *  @author  Burak Emir
 */
object Attribute {
  def unapply(x: Attribute): Option[(Nullable[String], Nullable[Seq[Node]], MetaData)] = x match {
    case PrefixedAttribute(_, key, value, next) => Some((key, value, next))
    case UnprefixedAttribute(key, value, next)  => Some((key, value, next))
    case _                                      => None
  }

  /** Convenience functions which choose Un/Prefixedness appropriately */
  def apply(key: Nullable[String], value: Seq[Node], next: MetaData): Attribute =
    new UnprefixedAttribute(key, value, next)

  def apply(pre: Nullable[String], key: String, value: String, next: MetaData): Attribute =
    if (pre == null || pre == "") new UnprefixedAttribute(key, value, next)
    else new PrefixedAttribute(pre, key, value, next)

  def apply(pre: Nullable[String], key: String, value: Seq[Node], next: MetaData): Attribute =
    if (pre == null || pre == "") new UnprefixedAttribute(key, value, next)
    else new PrefixedAttribute(pre, key, value, next)

  def apply(pre: Option[String], key: String, value: Seq[Node], next: MetaData): Attribute =
    pre match {
      case None    => new UnprefixedAttribute(key, value, next)
      case Some(p) => new PrefixedAttribute(p, key, value, next)
    }
}

/**
 * The `Attribute` trait defines the interface shared by both
 *  [[scala.xml.PrefixedAttribute]] and [[scala.xml.UnprefixedAttribute]].
 *
 *  @author  Burak Emir
 */
trait Attribute extends MetaData with ScalaVersionSpecificMetaData {
  def pre: Nullable[String] // will be null if unprefixed
  override val key: Nullable[String]
  override val value: Nullable[ScalaVersionSpecific.SeqOfNode]
  override val next: MetaData

  override def apply(key: String): Nullable[ScalaVersionSpecific.SeqOfNode]
  override def apply(namespace: Nullable[String], scope: NamespaceBinding, key: Nullable[String]): Nullable[ScalaVersionSpecific.SeqOfNode]
  override def copy(next: MetaData): Attribute

  override def remove(key: Nullable[String]): MetaData =
    if (!isPrefixed && this.key == key) next
    else copy(next.remove(key))

  override def remove(namespace: Nullable[String], scope: NamespaceBinding, key: String): MetaData =
    if (this.key == key && scope.getURI(pre) == namespace) next
    else copy(next.remove(namespace, scope, key))

  override def isPrefixed: Boolean = pre != null

  override def getNamespace(owner: Node): Nullable[String]

  override def wellformed(scope: NamespaceBinding): Boolean = {
    val arg: Nullable[String] = if (isPrefixed) scope.getURI(pre) else null
    (next(arg, scope, key) == null) && next.wellformed(scope)
  }

  /** Returns an iterator on attributes */
  override def iterator: Iterator[MetaData] =
    if (value == null) next.iterator
    else Iterator.single(this) ++ next.iterator

  override def size: Int =
    if (value == null) next.size
    else 1 + next.size

  /**
   * Appends string representation of only this attribute to stringbuffer.
   */
  override protected def toString1(sb: StringBuilder): Unit = {
    if (value == null)
      return
    if (isPrefixed)
      sb.append(s"$pre:")

    sb.append(s"$key=")
    val sb2: StringBuilder = new StringBuilder()
    Utility.sequenceToXML(value, TopScope, sb2, stripComments = true)
    Utility.appendQuoted(sb2.toString, sb)
  }
}
