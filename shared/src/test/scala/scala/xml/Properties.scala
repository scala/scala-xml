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

object Properties extends util.PropertiesTrait {
  override protected def propCategory    = "scala-xml"
  override protected def pickJarBasedOn  = classOf[scala.xml.Node]
}
