/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml

import scala.collection.{ mutable, immutable, AbstractSeq }
import mutable.{ Builder, ListBuffer }
import ScalaVersionSpecific.CBF
import scala.language.implicitConversions
import scala.collection.Seq

/**
 * This object ...
 *
 *  @author  Burak Emir
 */
object NodeSeq {
  final val Empty = fromSeq(Nil)
  def fromSeq(s: Seq[Node]): NodeSeq = new NodeSeq {
    def theSeq = s
  }

  // ---
  // For 2.11 / 2.12 only. Moving the implicit to a parent trait of `object NodeSeq` and keeping it
  // in ScalaVersionSpecific doesn't work because the implicit becomes less specific, which leads to
  // ambiguities.
  type Coll = NodeSeq
  implicit def canBuildFrom: CBF[Coll, Node, NodeSeq] = ScalaVersionSpecific.NodeSeqCBF
  // ---

  def newBuilder: Builder[Node, NodeSeq] = new ListBuffer[Node] mapResult fromSeq
  implicit def seqToNodeSeq(s: Seq[Node]): NodeSeq = fromSeq(s)
}

/**
 * This class implements a wrapper around `Seq[Node]` that adds XPath
 *  and comprehension methods.
 *
 *  @author  Burak Emir
 */
abstract class NodeSeq extends AbstractSeq[Node] with immutable.Seq[Node] with ScalaVersionSpecificNodeSeq with Equality with Serializable {
  def theSeq: Seq[Node]
  def length = theSeq.length
  override def iterator = theSeq.iterator

  def apply(i: Int): Node = theSeq(i)
  def apply(f: Node => Boolean): NodeSeq = filter(f)

  def xml_sameElements[A](that: Iterable[A]): Boolean = {
    val these = this.iterator
    val those = that.iterator
    while (these.hasNext && those.hasNext)
      if (these.next xml_!= those.next)
        return false

    !these.hasNext && !those.hasNext
  }

  protected def basisForHashCode: Seq[Any] = theSeq

  override def canEqual(other: Any) = other match {
    case _: NodeSeq => true
    case _          => false
  }

  override def strict_==(other: Equality) = other match {
    case x: NodeSeq => (length == x.length) && (theSeq sameElements x.theSeq)
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
    def fail = throw new IllegalArgumentException(that)
    def atResult = {
      lazy val y = this(0)
      val attr =
        if (that.length == 1) fail
        else if (that(1) == '{') {
          val i = that indexOf '}'
          if (i == -1) fail
          val (uri, key) = (that.substring(2, i), that.substring(i + 1, that.length()))
          if (uri == "" || key == "") fail
          else y.attribute(uri, key)
        } else y.attribute(that drop 1)

      attr match {
        case Some(x) => Group(x)
        case _       => NodeSeq.Empty
      }
    }

    def makeSeq(cond: (Node) => Boolean) =
      NodeSeq fromSeq (this flatMap (_.child) filter cond)

    that match {
      case ""                                        => fail
      case "_"                                       => makeSeq(!_.isAtom)
      case _ if (that(0) == '@' && this.length == 1) => atResult
      case _                                         => makeSeq(_.label == that)
    }
  }

  /**
   * Projection function, which returns elements of `this` sequence and of
   *  all its subsequences, based on the string `that`. Use:
   *   - `this \\ "foo" to get a list of all elements that are labelled with `"foo"`,
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
    def fail = throw new IllegalArgumentException(that)
    def filt(cond: (Node) => Boolean) = this flatMap (_.descendant_or_self) filter cond
    that match {
      case ""                  => fail
      case "_"                 => filt(!_.isAtom)
      case _ if that(0) == '@' => filt(!_.isAtom) flatMap (_ \ that)
      case _                   => filt(x => !x.isAtom && x.label == that)
    }
  }

  /**
   * Convenience method which returns string text of the named attribute. Use:
   *   - `that \@ "foo"` to get the string text of attribute `"foo"`;
   */
  def \@(attributeName: String): String = (this \ ("@" + attributeName)).text

  override def toString(): String = theSeq.mkString

  def text: String = (this map (_.text)).mkString
}
