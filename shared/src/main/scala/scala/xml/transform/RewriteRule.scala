/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2019, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package transform

import scala.collection.Seq

/**
 * A RewriteRule, when applied to a term, yields either
 *  the result of rewriting the term or the term itself if the rule
 *  is not applied.
 *
 *  @author  Burak Emir
 */
abstract class RewriteRule extends BasicTransformer {
  override def transform(ns: Seq[Node]): Seq[Node] = super.transform(ns)
  override def transform(n: Node): Seq[Node] = n
}

