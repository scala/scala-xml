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
import xml.Nullables._

/**
 * The class `NamespaceBinding` represents namespace bindings
 *  and scopes. The binding for the default namespace is treated as a null
 *  prefix. the absent namespace is represented with the null uri. Neither
 *  prefix nor uri may be empty, which is not checked.
 *
 *  @author  Burak Emir
 */
// Note: used by the Scala compiler.
@SerialVersionUID(0 - 2518644165573446725L)
case class NamespaceBinding(prefix: Nullable[String], uri: Nullable[String], parent: Nullable[NamespaceBinding]) extends AnyRef with Equality {
  if (prefix == "")
    throw new IllegalArgumentException("zero length prefix not allowed")

  def getURI(prefix: Nullable[String]): Nullable[String] =
    if (this.prefix == prefix) uri else parent.nn.getURI(prefix)

  /**
   * Returns some prefix that is mapped to the URI.
   *
   * @param _uri the input URI
   * @return the prefix that is mapped to the input URI, or null
   * if no prefix is mapped to the URI.
   */
  def getPrefix(uri: String): Nullable[String] =
    if (uri == this.uri) prefix else parent.nn.getPrefix(uri)

  override def toString: String = Utility.sbToString(buildString(_, TopScope))

  private def shadowRedefined(stop: NamespaceBinding): NamespaceBinding = {
    def prefixList(x: Nullable[NamespaceBinding]): List[Nullable[String]] =
      if ((x == null) || x.eq(stop)) Nil
      else x.prefix :: prefixList(x.parent)
    def fromPrefixList(l: List[Nullable[String]]): NamespaceBinding = l match {
      case Nil     => stop
      case x :: xs => NamespaceBinding(x, this.getURI(x), fromPrefixList(xs))
    }
    val ps0: List[Nullable[String]] = prefixList(this).reverse
    val ps: List[Nullable[String]] = ps0.distinct
    if (ps.size == ps0.size) this
    else fromPrefixList(ps)
  }

  override def canEqual(other: Any): Boolean = other match {
    case _: NamespaceBinding => true
    case _                   => false
  }

  override def strict_==(other: Equality): Boolean = other match {
    case x: NamespaceBinding => (prefix == x.prefix) && (uri == x.uri) && (parent == x.parent)
    case _                   => false
  }

  override def basisForHashCode: Seq[Any] = List(prefix, uri, parent)

  def buildString(stop: NamespaceBinding): String = Utility.sbToString(buildString(_, stop))

  def buildString(sb: StringBuilder, stop: NamespaceBinding): Unit =
    shadowRedefined(stop).doBuildString(sb, stop)

  private def doBuildString(sb: StringBuilder, stop: NamespaceBinding): Unit = {
    if (List(null, stop, TopScope).contains(this)) return

    val prefixStr: String = if (prefix != null) s":$prefix" else ""
    val uriStr: String = if (uri != null) uri else ""
    parent.nn.doBuildString(sb.append(s""" xmlns$prefixStr="$uriStr""""), stop) // copy(ignore)
  }
}
