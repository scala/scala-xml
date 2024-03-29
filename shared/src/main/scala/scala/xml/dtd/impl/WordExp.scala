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
 *  The class `WordExp` provides regular word expressions.
 *
 *  Users have to instantiate type member `_regexpT <;: RegExp`
 *  (from class `Base`) and a type member `_labelT <;: Label`.
 *
 *  Here is a short example:
 *  {{{
 *  import scala.util.regexp._
 *  import scala.util.automata._
 *  object MyLang extends WordExp {
 *    type _regexpT = RegExp
 *    type _labelT = MyChar
 *
 *    case class MyChar(c:Char) extends Label
 *  }
 *  import MyLang._
 *  // (a* | b)*
 *  val rex = Star(Alt(Star(Letter(MyChar('a'))),Letter(MyChar('b'))))
 *  object MyBerriSethi extends WordBerrySethi {
 *    override val lang = MyLang
 *  }
 *  val nfa = MyBerriSethi.automatonFrom(Sequ(rex), 1)
 *  }}}
 *
 *  @author  Burak Emir
 */
// TODO: still used in ContentModel -- @deprecated("This class will be removed", "2.10.0")
private[dtd] abstract class WordExp extends Base {

  abstract class Label

  override type _regexpT <: RegExp
  type _labelT <: Label

  case class Letter(a: _labelT) extends RegExp {
    final override lazy val isNullable: Boolean = false
    var pos: Int = -1
  }

  case class Wildcard() extends RegExp {
    final override lazy val isNullable: Boolean = false
    var pos: Int = -1
  }
}
