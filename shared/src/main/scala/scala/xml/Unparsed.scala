/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2020, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    (c) 2011-2020, Lightbend, Inc.       **
** /____/\___/_/ |_/____/_/ | |    http://scala-lang.org/               **
**                          |/                                          **
\*                                                                      */

package scala
package xml

/** An XML node for unparsed content. It will be output verbatim, all bets
  *  are off regarding wellformedness etc.
  *
  *  @author Burak Emir
  *  @param data content in this node, may not be null.
  */
class Unparsed(data: String) extends Atom[String](data) {

  /** Returns text, with some characters escaped according to XML
    *  specification.
    */
  override def buildString(sb: StringBuilder): StringBuilder =
    sb append data
}

/** This singleton object contains the `apply`and `unapply` methods for
  *  convenient construction and deconstruction.
  *
  *  @author  Burak Emir
  */
object Unparsed {
  def apply(data: String) = new Unparsed(data)
  def unapply(x: Unparsed) = Some(x.data)
}
