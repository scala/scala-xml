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

import java.net.URL
import java.io.File.separator

import scala.io.Source

/**  @author  Burak Emir
  */
trait ExternalSources {
  self: ExternalSources with MarkupParser with MarkupHandler =>

  def externalSource(systemId: String): Source = {
    if (systemId startsWith "http:")
      return Source fromURL new URL(systemId)

    val fileStr: String = input.descr match {
      case x if x startsWith "file:" => x drop 5
      case x                         => x take ((x lastIndexOf separator) + 1)
    }

    Source.fromFile(fileStr + systemId)
  }
}
