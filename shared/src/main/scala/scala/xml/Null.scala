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
import scala.collection.Iterator
import scala.collection.Seq

/**
 * Essentially, every method in here is a dummy, returning Zero[T].
 *  It provides a backstop for the unusual collection defined by MetaData,
 *  sort of a linked list of tails.
 *
 *  @author  Burak Emir
 */
// Note: used by the Scala compiler.
case object Null extends MetaData {
  override def iterator: Iterator[Nothing] = Iterator.empty
  override def size: Int = 0
  override def append(m: MetaData, scope: NamespaceBinding = TopScope): MetaData = m
  override def filter(f: MetaData => Boolean): ScalaVersionSpecificReturnTypes.NullFilter = this

  override def copy(next: MetaData): MetaData = next
  override def getNamespace(owner: Node): ScalaVersionSpecificReturnTypes.NullGetNamespace = null.asInstanceOf[ScalaVersionSpecificReturnTypes.NullGetNamespace]

  override def hasNext: Boolean = false
  override def next: ScalaVersionSpecificReturnTypes.NullNext = null.asInstanceOf[ScalaVersionSpecificReturnTypes.NullNext]
  override def key: ScalaVersionSpecificReturnTypes.NullKey = null.asInstanceOf[ScalaVersionSpecificReturnTypes.NullKey]
  override def value: ScalaVersionSpecificReturnTypes.NullValue = null.asInstanceOf[ScalaVersionSpecificReturnTypes.NullValue]
  override def isPrefixed: Boolean = false

  override def length: Int = 0
  override def length(i: Int): Int = i

  override def strict_==(other: Equality): Boolean = other match {
    case x: MetaData => x.length == 0
    case _           => false
  }
  override protected def basisForHashCode: Seq[Any] = Nil

  override def apply(namespace: Nullable[String], scope: NamespaceBinding, key: Nullable[String]): ScalaVersionSpecificReturnTypes.NullApply3 = null.asInstanceOf[ScalaVersionSpecificReturnTypes.NullApply3]
  override def apply(key: String): Nullable[ScalaVersionSpecific.SeqOfNode] =
    if (Utility.isNameStart(key.head)) null
    else throw new IllegalArgumentException(s"not a valid attribute name '$key', so can never match !")

  override protected def toString1(sb: StringBuilder): Unit = ()
  override protected def toString1: String = ""

  override def toString: String = ""

  override def buildString(sb: StringBuilder): StringBuilder = sb

  override def wellformed(scope: NamespaceBinding): Boolean = true

  override def remove(key: Nullable[String]): ScalaVersionSpecificReturnTypes.NullRemove = this
  override def remove(namespace: Nullable[String], scope: NamespaceBinding, key: String): ScalaVersionSpecificReturnTypes.NullRemove = this
}
