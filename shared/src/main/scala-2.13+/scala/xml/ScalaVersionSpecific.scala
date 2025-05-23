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

package scala.xml

import scala.collection.immutable.StrictOptimizedSeqOps
import scala.collection.{View, SeqOps, IterableOnce, immutable, mutable}
import scala.collection.BuildFrom
import scala.collection.mutable.Builder

private[xml] object ScalaVersionSpecific {
  import NodeSeq.Coll
  type CBF[-From, -A, +C] = BuildFrom[From, A, C]
  object NodeSeqCBF extends BuildFrom[Coll, Node, NodeSeq] {
    def newBuilder(from: Coll): Builder[Node, NodeSeq] = NodeSeq.newBuilder
    def fromSpecific(from: Coll)(it: IterableOnce[Node]): NodeSeq = (NodeSeq.newBuilder ++= from).result()
  }
  type SeqOfNode = scala.collection.immutable.Seq[Node]
  type SeqOfText = scala.collection.immutable.Seq[Text]
}

private[xml] trait ScalaVersionSpecificNodeSeq
  extends SeqOps[Node, immutable.Seq, NodeSeq]
    with StrictOptimizedSeqOps[Node, immutable.Seq, NodeSeq] { self: NodeSeq =>
  override def fromSpecific(coll: IterableOnce[Node]): NodeSeq = (NodeSeq.newBuilder ++= coll).result()
  override def newSpecificBuilder: mutable.Builder[Node, NodeSeq] = NodeSeq.newBuilder
  override def empty: NodeSeq = NodeSeq.Empty
  def concat(suffix: IterableOnce[Node]): NodeSeq =
    fromSpecific(iterator ++ suffix.iterator)
  @inline final def ++ (suffix: Seq[Node]): NodeSeq = concat(suffix)
  def appended(base: Node): NodeSeq =
    fromSpecific(new View.Appended(this, base))
  def appendedAll(suffix: IterableOnce[Node]): NodeSeq =
    concat(suffix)
  def prepended(base: Node): NodeSeq =
    fromSpecific(new View.Prepended(base, this))
  def prependedAll(prefix: IterableOnce[Node]): NodeSeq =
    fromSpecific(prefix.iterator ++ iterator)
  def map(f: Node => Node): NodeSeq =
    fromSpecific(new View.Map(this, f))
  def flatMap(f: Node => IterableOnce[Node]): NodeSeq =
    fromSpecific(new View.FlatMap(this, f))

  def theSeq: scala.collection.Seq[Node]
}

private[xml] trait ScalaVersionSpecificNodeBuffer { self: NodeBuffer =>
  override def className: String = "NodeBuffer"
}

private[xml] trait ScalaVersionSpecificNode { self: Node =>
  // These methods are overridden in Node with return type `immutable.Seq`. The declarations here result
  // in a bridge method in `Node` with result type `collection.Seq` which is needed for binary compatibility.
  def child: scala.collection.Seq[Node]
  def nonEmptyChildren: scala.collection.Seq[Node]
}

private[xml] trait ScalaVersionSpecificMetaData { self: MetaData =>
  def apply(key: String): scala.collection.Seq[Node]
  def apply(namespace_uri: String, owner: Node, key: String): scala.collection.Seq[Node]
  def apply(namespace_uri: String, scp: NamespaceBinding, k: String): scala.collection.Seq[Node]

  def value: scala.collection.Seq[Node]
}

private[xml] trait ScalaVersionSpecificTextBuffer { self: TextBuffer =>
  def toText: scala.collection.Seq[Text]
}

private[xml] trait ScalaVersionSpecificUtility { self: Utility.type =>
  def trimProper(x: Node): scala.collection.Seq[Node]
  def parseAttributeValue(value: String): scala.collection.Seq[Node]
}
