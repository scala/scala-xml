package scala.xml

import scala.collection.immutable.StrictOptimizedSeqOps
import scala.collection.{SeqOps, IterableOnce, immutable, mutable}
import scala.collection.BuildFrom
import scala.collection.mutable.Builder

private[xml] object ScalaVersionSpecific {
  import NodeSeq.Coll
  type CBF[-From, -A, +C] = BuildFrom[From, A, C]
  object NodeSeqCBF extends BuildFrom[Coll, Node, NodeSeq] {
    def newBuilder(from: Coll): Builder[Node, NodeSeq] = NodeSeq.newBuilder
    def fromSpecific(from: Coll)(it: IterableOnce[Node]): NodeSeq = (NodeSeq.newBuilder ++= from).result()
  }
}

private[xml] trait ScalaVersionSpecificNodeSeq
  extends SeqOps[Node, immutable.Seq, NodeSeq]
    with StrictOptimizedSeqOps[Node, immutable.Seq, NodeSeq] { self: NodeSeq =>
  override def fromSpecific(coll: IterableOnce[Node]): NodeSeq = (NodeSeq.newBuilder ++= coll).result()
  override def newSpecificBuilder: mutable.Builder[Node, NodeSeq] = NodeSeq.newBuilder
}

private[xml] trait ScalaVersionSpecificNodeBuffer { self: NodeBuffer =>
  override def className: String = "NodeBuffer"
}

private[xml] trait ScalaVersionSpecificIterableSerializable[+A] extends Iterable[A] {
  protected[this] override def writeReplace(): AnyRef = this
}
