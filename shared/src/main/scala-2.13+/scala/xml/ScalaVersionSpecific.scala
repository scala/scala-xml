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
}

private[xml] trait ScalaVersionSpecificNodeBuffer { self: NodeBuffer =>
  override def className: String = "NodeBuffer"
}
