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
 * Basic regular expressions.
 *
 *  @author  Burak Emir
 */

@deprecated("This class will be removed", "2.10.0")
private[dtd] abstract class Base {
  type _regexpT <: RegExp

  abstract class RegExp {
    def isNullable: Boolean
  }

  object Alt {
    /** `Alt( R,R,R* )`. */
    def apply(rs: _regexpT*): Alt =
      if (rs.size < 2) throw new SyntaxError("need at least 2 branches in Alt")
      else new Alt(rs: _*)
    // Can't enforce that statically without changing the interface
    // def apply(r1: _regexpT, r2: _regexpT, rs: _regexpT*) = new Alt(Seq(r1, r2) ++ rs: _*)
    def unapplySeq(x: Alt): Some[Seq[_regexpT]] = Some(x.rs)
  }

  class Alt private (val rs: _regexpT*) extends RegExp {
    final override val isNullable: Boolean = rs.exists(_.isNullable)
  }

  object Sequ {
    /** Sequ( R,R* ) */
    def apply(rs: _regexpT*): RegExp = if (rs.isEmpty) Eps else new Sequ(rs: _*)
    def unapplySeq(x: Sequ): Some[Seq[_regexpT]] = Some(x.rs)
  }

  class Sequ private (val rs: _regexpT*) extends RegExp {
    final override val isNullable: Boolean = rs.forall(_.isNullable)
  }

  case class Star(r: _regexpT) extends RegExp {
    final override lazy val isNullable: Boolean = true
  }

  // The empty Sequ.
  case object Eps extends RegExp {
    final override lazy val isNullable: Boolean = true
    override def toString: String = "Eps"
  }

  /** this class can be used to add meta information to regexps. */
  class Meta(r1: _regexpT) extends RegExp {
    final override val isNullable: Boolean = r1.isNullable
    def r: _regexpT = r1
  }
}
