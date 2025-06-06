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

import scala.collection.{mutable, immutable, AbstractSeq}
import ScalaVersionSpecific.CBF
import scala.language.implicitConversions
import scala.collection.Seq

/**
 * This object ...
 *
 *  @author  Burak Emir
 */
object NodeSeq {
  final val Empty: NodeSeq = fromSeq(Nil)
  def fromSeq(s: Seq[Node]): NodeSeq = new NodeSeq {
    override val theSeq: ScalaVersionSpecific.SeqOfNode = s match {
      case ns: ScalaVersionSpecific.SeqOfNode => ns
      case _ => s.toVector
    }
  }

  // ---
  // For 2.11 / 2.12 only. Moving the implicit to a parent trait of `object NodeSeq` and keeping it
  // in ScalaVersionSpecific doesn't work because the implicit becomes less specific, which leads to
  // ambiguities.
  type Coll = NodeSeq
  implicit def canBuildFrom: CBF[Coll, Node, NodeSeq] = ScalaVersionSpecific.NodeSeqCBF
  // ---

  def newBuilder: mutable.Builder[Node, NodeSeq] = new mutable.ListBuffer[Node].mapResult(fromSeq)
  implicit def seqToNodeSeq(s: Seq[Node]): NodeSeq = fromSeq(s)
}

/**
 * This class implements a wrapper around `Seq[Node]` that adds XPath
 *  and comprehension methods.
 *
 *  @author  Burak Emir
 */
abstract class NodeSeq extends AbstractSeq[Node] with immutable.Seq[Node] with ScalaVersionSpecificNodeSeq with Equality with Serializable {
  def theSeq: ScalaVersionSpecific.SeqOfNode
  override def length: Int = theSeq.length
  override def iterator: Iterator[Node] = theSeq.iterator

  override def apply(i: Int): Node = theSeq(i)
  def apply(f: Node => Boolean): NodeSeq = filter(f)

  def xml_sameElements[A](that: Iterable[A]): Boolean = {
    val these: Iterator[Node] = this.iterator
    val those: Iterator[A] = that.iterator
    while (these.hasNext && those.hasNext)
      if (these.next().xml_!=(those.next()))
        return false

    !these.hasNext && !those.hasNext
  }

  override protected def basisForHashCode: Seq[Any] = theSeq

  override def canEqual(other: Any): Boolean = other match {
    case _: NodeSeq => true
    case _          => false
  }

  override def strict_==(other: Equality): Boolean = other match {
    case x: NodeSeq => (length == x.length) && theSeq.sameElements(x.theSeq)
    case _          => false
  }

  /**
   * Projection function, which returns  elements of `this` sequence based
   *  on the string `that`. Use:
   *   - `this \ "foo"` to get a list of all children that are labelled with `"foo"`;
   *   - `this \ "_"` to get a list of all child elements (wildcard);
   *   - `this \ "@foo"` to get the unprefixed attribute `"foo"` of `this`;
   *   - `this \ "@{uri}foo"` to get the prefixed attribute `"pre:foo"` whose
   *     prefix `"pre"` is resolved to the namespace `"uri"`.
   *
   *  For attribute projections, the resulting [[scala.xml.NodeSeq]] attribute
   *  values are wrapped in a [[scala.xml.Group]].
   *
   *  There is no support for searching a prefixed attribute by its literal prefix.
   *
   *  The document order is preserved.
   */
  def \(that: String): NodeSeq = {
    def fail: Nothing = throw new IllegalArgumentException(that)
    def atResult: NodeSeq = {
      lazy val y: Node = this(0)
      val attr: Option[Seq[Node]] =
        if (that.length == 1) fail
        else if (that(1) == '{') {
          val i: Int = that.indexOf('}')
          if (i == -1) fail
          val (uri: String, key: String) = (that.substring(2, i), that.substring(i + 1, that.length))
          if (uri.isEmpty || key.isEmpty) fail
          else y.attribute(uri, key)
        } else y.attribute(that.drop(1))

      attr match {
        case Some(x) => Group(x)
        case _       => NodeSeq.Empty
      }
    }

    def makeSeq(cond: Node => Boolean): NodeSeq =
      NodeSeq.fromSeq(this.flatMap(_.child).filter(cond))

    that match {
      case ""                                      => fail
      case "_"                                     => makeSeq(!_.isAtom)
      case "@"                                     => fail
      case _ if that(0) == '@' && this.length == 1 => atResult
      case _                                       => makeSeq(_.label == that)
    }
  }

  /**
   * Projection function, which returns elements of `this` sequence and of
   *  all its subsequences, based on the string `that`. Use:
   *   - `this \\ "foo"` to get a list of all elements that are labelled with "foo"`,
   *     including `this`;
   *   - `this \\ "_"` to get a list of all elements (wildcard), including `this`;
   *   - `this \\ "@foo"` to get all unprefixed attributes `"foo"`;
   *   - `this \\ "@{uri}foo"` to get all prefixed attribute `"pre:foo"` whose
   *     prefix `"pre"` is resolved to the namespace `"uri"`.
   *
   *  For attribute projections, the resulting [[scala.xml.NodeSeq]] attribute
   *  values are wrapped in a [[scala.xml.Group]].
   *
   *  There is no support for searching a prefixed attribute by its literal prefix.
   *
   *  The document order is preserved.
   */
  def \\(that: String): NodeSeq = {
    def fail: Nothing = throw new IllegalArgumentException(that)
    def filt(cond: Node => Boolean): NodeSeq = this.flatMap(_.descendant_or_self).filter(cond)
    that match {
      case ""                  => fail
      case "_"                 => filt(!_.isAtom)
      case _ if that(0) == '@' => filt(!_.isAtom).flatMap(_ \ that)
      case _                   => filt(x => !x.isAtom && x.label == that)
    }
  }

  /**
   * Convenience method which returns string text of the named attribute. Use:
   *   - `that \@ "foo"` to get the string text of attribute `"foo"`;
   */
  def \@(attributeName: String): String = (this \ s"@$attributeName").text

  override def toString: String = theSeq.mkString

  def text: String = this.map(_.text).mkString
}
