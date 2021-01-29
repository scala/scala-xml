/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2020, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    (c) 2011-2020, Lightbend, Inc.       **
** /____/\___/_/ |_/____/_/ | |    http://scala-lang.org/               **
**                          |/                                          **
\*                                                                      */

package scala
package xml
package transform

import scala.collection.Seq

class RuleTransformer(rules: RewriteRule*) extends BasicTransformer {
  private val transformers = rules.map(new NestingTransformer(_))
  override def transform(n: Node): Seq[Node] = {
    if (transformers.isEmpty) n
    else
      transformers.tail.foldLeft(transformers.head.transform(n)) {
        (res, transformer) => transformer.transform(res)
      }
  }
}
