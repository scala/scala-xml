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

import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom

private[xml] object ScalaVersionSpecific {
  import NodeSeq.Coll
  type CBF[-From, -A, +C] = CanBuildFrom[From, A, C]
  object NodeSeqCBF extends CanBuildFrom[Coll, Node, NodeSeq] {
    def apply(from: Coll) = NodeSeq.newBuilder
    def apply() = NodeSeq.newBuilder
  }
}

private[xml] trait ScalaVersionSpecificNodeSeq extends SeqLike[Node, NodeSeq] { self: NodeSeq =>
  /** Creates a list buffer as builder for this class */
  override protected[this] def newBuilder = NodeSeq.newBuilder
}

private[xml] trait ScalaVersionSpecificNodeBuffer { self: NodeBuffer =>
  override def stringPrefix: String = "NodeBuffer"
}
