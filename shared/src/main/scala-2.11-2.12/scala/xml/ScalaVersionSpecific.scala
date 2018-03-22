package scala.xml

import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom

object ScalaVersionSpecific {
  import NodeSeq.Coll
  type CBF[-From, -A, +C] = CanBuildFrom[From, A, C]
  object NodeSeqCBF extends CanBuildFrom[Coll, Node, NodeSeq] {
    def apply(from: Coll) = NodeSeq.newBuilder
    def apply() = NodeSeq.newBuilder
  }
}

trait ScalaVersionSpecificNodeSeq extends SeqLike[Node, NodeSeq] { self: NodeSeq =>
  /** Creates a list buffer as builder for this class */
  override protected[this] def newBuilder = NodeSeq.newBuilder
}

trait ScalaVersionSpecificNodeBuffer { self: NodeBuffer =>
  override def stringPrefix: String = "NodeBuffer"
}
