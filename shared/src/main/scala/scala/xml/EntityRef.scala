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

/**
 * The class `EntityRef` implements an XML node for entity references.
 *
 * @author  Burak Emir
 * @param   entityName the name of the entity reference, for example `amp`.
 */
case class EntityRef(entityName: String) extends SpecialNode {
  final override def doCollectNamespaces = false
  final override def doTransform = false
  override def label = "#ENTITY"

  override def text = entityName match {
    case "lt"   => "<"
    case "gt"   => ">"
    case "amp"  => "&"
    case "apos" => "'"
    case "quot" => "\""
    case _      => Utility.sbToString(buildString)
  }

  /**
   * Appends `"&amp; entityName;"` to this string buffer.
   *
   *  @param  sb the string buffer.
   *  @return the modified string buffer `sb`.
   */
  override def buildString(sb: StringBuilder) =
    sb.append("&").append(entityName).append(";")

}
