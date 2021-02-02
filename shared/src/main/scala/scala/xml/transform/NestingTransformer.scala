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
package xml
package transform

import scala.collection.Seq

class NestingTransformer(rule: RewriteRule) extends BasicTransformer {
  override def transform(n: Node): Seq[Node] = {
    rule.transform(super.transform(n))
  }
}
