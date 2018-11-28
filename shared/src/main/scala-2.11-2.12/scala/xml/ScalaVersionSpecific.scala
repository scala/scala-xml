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

private[xml] trait ScalaVersionSpecificIterableSerializable[+A] { // extends Iterable[A] {
  // protected[this] override def writeReplace(): AnyRef = this
}
