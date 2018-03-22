package scala.xml

import scala.collection.immutable.StrictOptimizedSeqOps
import scala.collection.{SeqOps, immutable, mutable}
import scala.collection.BuildFrom
import scala.collection.mutable.Builder

object ScalaVersionSpecific {
  import NodeSeq.Coll
  type CBF[-From, -A, +C] = BuildFrom[From, A, C]
  object NodeSeqCBF extends BuildFrom[Coll, Node, NodeSeq] {
    def newBuilder(from: Coll): Builder[Node, NodeSeq] = NodeSeq.newBuilder
    def fromSpecificIterable(from: Coll)(it: Iterable[Node]): NodeSeq = (NodeSeq.newBuilder ++= from).result()
  }
}

trait ScalaVersionSpecificNodeSeq
  extends SeqOps[Node, immutable.Seq, NodeSeq]
    with StrictOptimizedSeqOps[Node, immutable.Seq, NodeSeq] { self: NodeSeq =>
  override def fromSpecificIterable(coll: Iterable[Node]): NodeSeq = (NodeSeq.newBuilder ++= coll).result()

  override def newSpecificBuilder(): mutable.Builder[Node, NodeSeq] = NodeSeq.newBuilder
}

trait ScalaVersionSpecificNodeBuffer { self: NodeBuffer =>
  override def className: String = "NodeBuffer"
}
