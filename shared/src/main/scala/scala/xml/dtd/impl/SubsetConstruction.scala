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

// TODO: still used in ContentModel -- @deprecated("This class will be removed", "2.10.0")
private[dtd] class SubsetConstruction[T <: AnyRef](val nfa: NondetWordAutom[T]) {
  import nfa.labels

  def selectTag(Q: immutable.BitSet, finals: Array[Int]): Int =
    Q.map(finals).filter(_ > 0).min

  def determinize: DetWordAutom[T] = {
    // for assigning numbers to bitsets
    val indexMap: mutable.Map[immutable.BitSet, Int] = mutable.Map[immutable.BitSet, Int]()
    val invIndexMap: mutable.Map[Int, immutable.BitSet] = mutable.Map[Int, immutable.BitSet]()
    var ix: Int = 0

    // we compute the dfa with states = bitsets
    val q0: immutable.BitSet = immutable.BitSet(0) // the set { 0 }
    val sink: immutable.BitSet = immutable.BitSet.empty // the set { }

    var states: Set[immutable.BitSet] = Set(q0, sink) // initial set of sets
    val delta: mutable.HashMap[immutable.BitSet, mutable.HashMap[T, immutable.BitSet]] =
      new mutable.HashMap[immutable.BitSet, mutable.HashMap[T, immutable.BitSet]]
    val deftrans: mutable.Map[immutable.BitSet, immutable.BitSet] = mutable.Map(q0 -> sink, sink -> sink) // initial transitions
    val finals: mutable.Map[immutable.BitSet, Int] = mutable.Map()
    var rest: immutable.List[immutable.BitSet] = immutable.List.empty[immutable.BitSet]

    rest = q0 :: sink :: rest

    def addFinal(q: immutable.BitSet): Unit = {
      if (nfa.containsFinal(q))
        finals(q) = selectTag(q, nfa.finals)
    }
    def add(Q: immutable.BitSet): Unit = {
      if (!states(Q)) {
        states += Q
        rest = Q :: rest
        addFinal(Q)
      }
    }

    addFinal(q0) // initial state may also be a final state

    while (rest.nonEmpty) {
      val P: immutable.BitSet = rest.head
      rest = rest.tail
      // assign a number to this bitset
      indexMap(P) = ix
      invIndexMap(ix) = P
      ix += 1

      // make transition map
      val Pdelta: mutable.HashMap[T, immutable.BitSet] = new mutable.HashMap[T, immutable.BitSet]
      delta.update(P, Pdelta)

      labels.foreach { label =>
        val Q: immutable.BitSet = nfa.next(P, label)
        Pdelta.update(label, Q)
        add(Q)
      }

      // collect default transitions
      val Pdef: immutable.BitSet = nfa nextDefault P
      deftrans(P) = Pdef
      add(Pdef)
    }

    // create DetWordAutom, using indices instead of sets
    val nstatesR: Int = states.size
    val deltaR: Array[mutable.Map[T, Int]] = new Array[mutable.Map[T, Int]](nstatesR)
    val defaultR: Array[Int] = new Array[Int](nstatesR)
    val finalsR: Array[Int] = new Array[Int](nstatesR)

    for (Q <- states) {
      val q: Int = indexMap(Q)
      val trans: mutable.Map[T, immutable.BitSet] = delta(Q)
      val transDef: immutable.BitSet = deftrans(Q)
      val qDef: Int = indexMap(transDef)
      val ntrans: mutable.Map[T, Int] = new mutable.HashMap[T, Int]()

      for ((label, value) <- trans) {
        val p: Int = indexMap(value)
        if (p != qDef)
          ntrans.update(label, p)
      }

      deltaR(q) = ntrans
      defaultR(q) = qDef
    }

    finals.foreach { case (k, v) => finalsR(indexMap(k)) = v }

    new DetWordAutom[T] {
      override val nstates: Int = nstatesR
      override val delta: Array[mutable.Map[T, Int]] = deltaR
      override val default: Array[Int] = defaultR
      override val finals: Array[Int] = finalsR
    }
  }
}
