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

import scala.collection.Seq

/**
 * A fast test of language inclusion between minimal automata.
 *  inspired by the ''AMoRE automata library''.
 *
 *  @author Burak Emir
 */
@deprecated("This class will be removed", "2.10.0")
private[dtd] trait Inclusion[A <: AnyRef] {

  val labels: Seq[A]

  /**
   * Returns true if `dfa1` is included in `dfa2`.
   */
  def inclusion(dfa1: DetWordAutom[A], dfa2: DetWordAutom[A]): Boolean = {

    def encode(q1: Int, q2: Int): Int = 1 + q1 + q2 * dfa1.nstates
    def decode2(c: Int): Int = (c - 1) / dfa1.nstates //integer division
    def decode1(c: Int): Int = (c - 1) % dfa1.nstates

    var q1: Int = 0 //dfa1.initstate; // == 0
    var q2: Int = 0 //dfa2.initstate; // == 0

    val max: Int = 1 + dfa1.nstates * dfa2.nstates
    val mark: Array[Int] = new Array[Int](max)

    var result: Boolean = true
    var current: Int = encode(q1, q2)
    var last: Int = current
    mark(last) = max // mark (q1,q2)
    while (current != 0 && result) {
      //Console.println("current = [["+q1+" "+q2+"]] = "+current);
      for (letter <- labels) {
        val r1: Int = dfa1.next(q1, letter)
        val r2: Int = dfa2.next(q2, letter)
        if (dfa1.isFinal(r1) && !dfa2.isFinal(r2))
          result = false
        val test: Int = encode(r1, r2)
        //Console.println("test = [["+r1+" "+r2+"]] = "+test);
        if (mark(test) == 0) {
          mark(last) = test
          mark(test) = max
          last = test
        }
      }
      val ncurrent: Int = mark(current)
      if (ncurrent != max) {
        q1 = decode1(ncurrent)
        q2 = decode2(ncurrent)
        current = ncurrent
      } else {
        current = 0
      }
    }
    result
  }
}
