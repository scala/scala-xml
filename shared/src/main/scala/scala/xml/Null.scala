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

import scala.collection.Iterator
import scala.collection.Seq

/**
 * Essentially, every method in here is a dummy, returning Zero[T].
 *  It provides a backstop for the unusual collection defined by MetaData,
 *  sort of a linked list of tails.
 *
 *  @author  Burak Emir
 */
case object Null extends MetaData {
  override def iterator: Iterator[Nothing] = Iterator.empty
  override def size: Int = 0
  override def append(m: MetaData, scope: NamespaceBinding = TopScope): MetaData = m
  override def filter(f: MetaData => Boolean): Null.type = this

  override def copy(next: MetaData): MetaData = next
  override def getNamespace(owner: Node): scala.Null = null

  override def hasNext: Boolean = false
  override def next: scala.Null = null
  override def key: scala.Null = null
  override def value: scala.Null = null
  override def isPrefixed: Boolean = false

  override def length: Int = 0
  override def length(i: Int): Int = i

  override def strict_==(other: Equality): Boolean = other match {
    case x: MetaData => x.length == 0
    case _           => false
  }
  override protected def basisForHashCode: Seq[Any] = Nil

  override def apply(namespace: String, scope: NamespaceBinding, key: String): scala.Null = null

  override def apply(key: String): scala.Null =
    if (Utility.isNameStart(key.head)) null
    else throw new IllegalArgumentException("not a valid attribute name '" + key + "', so can never match !")

  override protected def toString1(sb: StringBuilder): Unit = ()
  override protected def toString1: String = ""

  override def toString: String = ""

  override def buildString(sb: StringBuilder): StringBuilder = sb

  override def wellformed(scope: NamespaceBinding): Boolean = true

  override def remove(key: String): Null.type = this
  override def remove(namespace: String, scope: NamespaceBinding, key: String): Null.type = this
}
