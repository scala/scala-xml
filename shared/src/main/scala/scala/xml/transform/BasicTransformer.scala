/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package transform

/**
 * A class for XML transformations.
 *
 *  @author  Burak Emir
 */
abstract class BasicTransformer extends Function1[Node, Node] {
  protected def unchanged(n: Node, ns: collection.Seq[Node]) =
    ns.length == 1 && (ns.head == n)

  /**
   * Call transform(Node) for each node in ns, append results
   *  to NodeBuffer.
   */
  def transform(it: Iterator[Node], nb: NodeBuffer): collection.Seq[Node] =
    it.foldLeft(nb)(_ ++= transform(_)).toSeq

  /**
   * Call transform(Node) to each node in ns, yield ns if nothing changes,
   *  otherwise a new sequence of concatenated results.
   */
  def transform(ns: collection.Seq[Node]): collection.Seq[Node] = {
    val changed = ns flatMap transform
    if (changed.length != ns.length || (changed, ns).zipped.exists(_ != _)) changed
    else ns
}

  def transform(n: Node): collection.Seq[Node] = {
    if (n.doTransform) n match {
      case Group(xs) => Group(transform(xs)) // un-group the hack Group tag
      case _ =>
        val ch = n.child
        val nch = transform(ch)

        if (ch eq nch) n
        else Elem(n.prefix, n.label, n.attributes, n.scope, nch.isEmpty, nch: _*)
    }
    else n
  }

  def apply(n: Node): Node = {
    val seq = transform(n)
    if (seq.length > 1)
      throw new UnsupportedOperationException("transform must return single node for root")
    else seq.head
  }
}
