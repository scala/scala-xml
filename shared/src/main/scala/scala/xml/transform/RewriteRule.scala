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
 * A RewriteRule, when applied to a term, yields either
 *  the result of rewriting the term or the term itself if the rule
 *  is not applied.
 *
 *  @author  Burak Emir
 */
abstract class RewriteRule extends BasicTransformer {
  /** a name for this rewrite rule */
  val name = this.toString()
  override def transform(ns: collection.Seq[Node]): collection.Seq[Node] = super.transform(ns)
  override def transform(n: Node): collection.Seq[Node] = n
}

