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
package parsing

import scala.io.Source

/**
 * An XML Parser that preserves `CDATA` blocks and knows about
 *  [[scala.xml.parsing.XhtmlEntities]].
 *
 *  @author (c) David Pollak, 2007 WorldWide Conferencing, LLC.
 */
class XhtmlParser(override val input: Source) extends ConstructingHandler with MarkupParser with ExternalSources {
  override val preserveWS: Boolean = true
  ent ++= XhtmlEntities()
}

/**
 * Convenience method that instantiates, initializes and runs an `XhtmlParser`.
 *
 *  @author Burak Emir
 */
object XhtmlParser {
  def apply(source: Source): NodeSeq = new XhtmlParser(source).initialize.document()
}
