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

  def isFinal(q: Int): Boolean = finals(q) != 0
  def isSink(q: Int): Boolean = delta(q).isEmpty && default(q) == q
  def next(q: Int, label: T): Int = delta(q).getOrElse(label, default(q))

  override def toString: String = {
    val sb: StringBuilder = new StringBuilder("[DetWordAutom  nstates=")
    sb.append(nstates)
    sb.append(" finals=")
    val map: Map[Int, Int] = finals.zipWithIndex.map(_.swap).toMap
    sb.append(map.toString)
    sb.append(" delta=\n")

    for (i <- 0.until(nstates)) {
      sb.append("%d->%s\n".format(i, delta(i)))
      if (i < default.length)
        sb.append("_>%s\n".format(default(i)))
    }
    sb.toString
  }
}
