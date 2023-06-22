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
 * The class `Comment` implements an XML node for comments.
 *
 * @author Burak Emir
 * @param commentText the text contained in this node, may not contain "--"
 *        and the final character may not be `-` to prevent a closing span of `-->`
 *        which is invalid. [[https://www.w3.org/TR/xml11//#IDA5CES]]
 */
// Note: used by the Scala compiler.
case class Comment(commentText: String) extends SpecialNode {

  override def label: String = "#REM"
  override def text: String = ""
  final override def doCollectNamespaces: Boolean = false
  final override def doTransform: Boolean = false

  if (commentText.contains("--")) {
    throw new IllegalArgumentException(s"""text contains "--"""")
  }
  if (commentText.nonEmpty && commentText.charAt(commentText.length - 1) == '-') {
    throw new IllegalArgumentException("The final character of a XML comment may not be '-'. See https://www.w3.org/TR/xml11//#IDA5CES")
  }

  /**
   * Appends &quot;<!-- text -->&quot; to this string buffer.
   */
  override def buildString(sb: StringBuilder): StringBuilder =
    sb.append(s"<!--$commentText-->")
}
