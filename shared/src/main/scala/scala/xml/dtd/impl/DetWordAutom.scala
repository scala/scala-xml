/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2020, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    (c) 2011-2020, Lightbend, Inc.       **
** /____/\___/_/ |_/____/_/ | |    http://scala-lang.org/               **
**                          |/                                          **
\*                                                                      */

package scala
package xml.dtd.impl

/**
 * A deterministic automaton. States are integers, where
 *  0 is always the only initial state. Transitions are represented
 *  in the delta function. A default transitions is one that
 *  is taken when no other transition can be taken.
 *  All states are reachable. Accepting states are those for which
 *  the partial function 'finals' is defined.
 *
 *  @author Burak Emir
 */
// TODO: still used in ContentModel -- @deprecated("This class will be removed", "2.10.0")
private[dtd] abstract class DetWordAutom[T <: AnyRef] {
  val nstates: Int
  val finals: Array[Int]
  val delta: Array[scala.collection.mutable.Map[T, Int]]
  val default: Array[Int]

  def isFinal(q: Int) = finals(q) != 0
  def isSink(q: Int) = delta(q).isEmpty && default(q) == q
  def next(q: Int, label: T) = delta(q).getOrElse(label, default(q))

  override def toString() = {
    val sb = new StringBuilder("[DetWordAutom  nstates=")
    sb.append(nstates)
    sb.append(" finals=")
    val map = finals.zipWithIndex.map(_.swap).toMap
    sb.append(map.toString())
    sb.append(" delta=\n")

    for (i <- 0 until nstates) {
      sb append "%d->%s\n".format(i, delta(i))
      if (i < default.length)
        sb append "_>%s\n".format(default(i))
    }
    sb.toString
  }
}
