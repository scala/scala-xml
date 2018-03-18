/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml

/**
 * `SpecialNode` is a special XML node which represents either text
 *  `(PCDATA)`, a comment, a `PI`, or an entity ref.
 *
 *  @author Burak Emir
 */
abstract class SpecialNode extends Node {

  /** always empty */
  final override def attributes = Null

  /** always Node.EmptyNamespace */
  final override def namespace = null

  /** always empty */
  final def child = Nil

  /** Append string representation to the given string buffer argument. */
  def buildString(sb: StringBuilder): StringBuilder
}
