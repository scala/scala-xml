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

import Utility.isNameStart
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
  override def iterator /* TODO type annotation */ = Iterator.empty
  override def size: Int = 0
  override def append(m: MetaData, scope: NamespaceBinding = TopScope): MetaData = m
  override def filter(f: MetaData => Boolean): MetaData = this

  override def copy(next: MetaData): MetaData = next
  override def getNamespace(owner: Node) /* TODO type annotation */ = null

  override def hasNext: Boolean = false
  override def next /* TODO type annotation */ = null
  override def key /* TODO type annotation */ = null
  override def value /* TODO type annotation */ = null
  override def isPrefixed: Boolean = false

  override def length: Int = 0
  override def length(i: Int): Int = i

  override def strict_==(other: Equality): Boolean = other match {
    case x: MetaData => x.length == 0
    case _           => false
  }
  override protected def basisForHashCode: Seq[Any] = Nil

  override def apply(namespace: String, scope: NamespaceBinding, key: String) /* TODO type annotation */ = null
  override def apply(key: String) /* TODO type annotation */ =
    if (isNameStart(key.head)) null
    else throw new IllegalArgumentException("not a valid attribute name '" + key + "', so can never match !")

  override protected def toString1(sb: StringBuilder): Unit = ()
  override protected def toString1(): String = ""

  override def toString(): String = ""

  override def buildString(sb: StringBuilder): StringBuilder = sb

  override def wellformed(scope: NamespaceBinding): Boolean = true

  override def remove(key: String) /* TODO type annotation */ = this
  override def remove(namespace: String, scope: NamespaceBinding, key: String) /* TODO type annotation */ = this
}
