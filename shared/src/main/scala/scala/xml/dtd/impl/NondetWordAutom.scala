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

import scala.collection.{immutable, mutable}
import scala.collection.Seq

/**
 * A nondeterministic automaton. States are integers, where
 *  0 is always the only initial state. Transitions are represented
 *  in the delta function. Default transitions are transitions that
 *  are taken when no other transitions can be applied.
 *  All states are reachable. Accepting states are those for which
 *  the partial function `finals` is defined.
 */
// TODO: still used in ContentModel -- @deprecated("This class will be removed", "2.10.0")
private[dtd] abstract class NondetWordAutom[T <: AnyRef] {
  val nstates: Int
  val labels: Seq[T]
  val finals: Array[Int] // 0 means not final
  val delta: Array[mutable.Map[T, immutable.BitSet]]
  val default: Array[immutable.BitSet]

  /** @return true if the state is final */
  final def isFinal(state: Int): Boolean = finals(state) > 0

  /** @return tag of final state */
  final def finalTag(state: Int): Int = finals(state)

  /** @return true if the set of states contains at least one final state */
  final def containsFinal(Q: immutable.BitSet): Boolean = Q.exists(isFinal)

  /** @return true if there are no accepting states */
  final def isEmpty: Boolean = 0.until(nstates).forall(x => !isFinal(x))

  /** @return a immutable.BitSet with the next states for given state and label */
  def next(q: Int, a: T): immutable.BitSet = delta(q).getOrElse(a, default(q))

  /** @return a immutable.BitSet with the next states for given state and label */
  def next(Q: immutable.BitSet, a: T): immutable.BitSet = next(Q, next(_, a))
  def nextDefault(Q: immutable.BitSet): immutable.BitSet = next(Q, default)

  private def next(Q: immutable.BitSet, f: Int => immutable.BitSet): immutable.BitSet =
    Q.toSet.map(f).foldLeft(immutable.BitSet.empty)(_ ++ _)

  private def finalStates: immutable.Seq[Int] = 0.until(nstates).filter(isFinal)
  override def toString: String = {
    val finalString: String = Map(finalStates.map(j => j -> finals(j)): _*).toString
    val deltaString: String = 0.until(nstates)
      .map(i => s"   $i->${delta(i)}\n    _>${default(i)}\n").mkString

    s"[NondetWordAutom  nstates=$nstates  finals=$finalString  delta=\n$deltaString"
  }
}
