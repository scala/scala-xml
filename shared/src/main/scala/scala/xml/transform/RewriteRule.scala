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

