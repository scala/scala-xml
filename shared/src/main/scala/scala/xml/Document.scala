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

import scala.collection.Seq

/**
 * A document information item (according to InfoSet spec). The comments
 *  are copied from the Infoset spec, only augmented with some information
 *  on the Scala types for definitions that might have no value.
 *  Also plays the role of an `XMLEvent` for pull parsing.
 *
 *  @author  Burak Emir
 */
@SerialVersionUID(-2289320563321795109L)
class Document extends NodeSeq with Serializable {

  /**
   * An ordered list of child information items, in document
   *  order. The list contains exactly one element information item. The
   *  list also contains one processing instruction information item for
   *  each processing instruction outside the document element, and one
   *  comment information item for each comment outside the document
   *  element. Processing instructions and comments within the DTD are
   *  excluded. If there is a document type declaration, the list also
   *  contains a document type declaration information item.
   */
  var children: Seq[Node] = _ // effectively an `immutable.Seq`, not changed due to binary compatibility

  /** The element information item corresponding to the document element. */
  var docElem: Node = _

  /** The dtd that comes with the document, if any */
  var dtd: scala.xml.dtd.DTD = _

  /**
   * An unordered set of notation information items, one for each notation
   *  declared in the DTD. If any notation is multiply declared, this property
   *  has no value.
   */
  def notations: Seq[scala.xml.dtd.NotationDecl] =
    dtd.notations

  /**
   * An unordered set of unparsed entity information items, one for each
   *  unparsed entity declared in the DTD.
   */
  def unparsedEntities: Seq[scala.xml.dtd.EntityDecl] =
    dtd.unparsedEntities

  /** The base URI of the document entity. */
  var baseURI: String = _

  /**
   * The name of the character encoding scheme in which the document entity
   *  is expressed.
   */
  var encoding: Option[String] = _

  /**
   * An indication of the standalone status of the document, either
   *  true or false. This property is derived from the optional standalone
   *  document declaration in the XML declaration at the beginning of the
   *  document entity, and has no value (`None`) if there is no
   *  standalone document declaration.
   */
  var standAlone: Option[Boolean] = _

  /**
   * A string representing the XML version of the document. This
   *  property is derived from the XML declaration optionally present at
   *  the beginning of the document entity, and has no value (`None`)
   *  if there is no XML declaration.
   */
  var version: Option[String] = _

  /**
   * 9. This property is not strictly speaking part of the infoset of
   *  the document. Rather it is an indication of whether the processor
   *  has read the complete DTD. Its value is a boolean. If it is false,
   *  then certain properties (indicated in their descriptions below) may
   *  be unknown. If it is true, those properties are never unknown.
   */
  var allDeclarationsProcessed: Boolean = false

  // methods for NodeSeq

  override def theSeq: ScalaVersionSpecific.SeqOfNode = this.docElem

  override def canEqual(other: Any): Boolean = other match {
    case _: Document => true
    case _           => false
  }
}
