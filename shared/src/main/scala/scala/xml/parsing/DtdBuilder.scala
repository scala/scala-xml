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

import scala.xml.dtd._

// Note: this is private to avoid it becoming a part of binary compatibility checks
final private[parsing] class DtdBuilder(
  name: String,
  externalID: ExternalID
) {
  private var elements: List[ElemDecl] = List.empty
  private var attributeLists: List[AttListDecl] = List.empty
  private var entities: List[EntityDecl] = List.empty
  private var notations: List[NotationDecl] = List.empty
  private var unparsedEntities: List[UnparsedEntityDecl] = List.empty
  private var parameterReferences: List[PEReference] = List.empty

  // AttListDecl under construction
  private var elementName: Option[String] = None
  private var attributes: List[AttrDecl] = List.empty

  private def flushAttributes(): Unit = if (elementName.isDefined) {
    attributeLists ::= AttListDecl(elementName.get, attributes.reverse)
    attributes = List.empty
    elementName = None
  }

  private var done: Boolean = false
  def isDone: Boolean = done

  def endDTD(): Unit = {
    flushAttributes()
    done = true
  }

  def dtd: DTD = new DTD {
    // Note: weirdly, unlike DocType, DTD does not have a 'name'...
    this.externalID = DtdBuilder.this.externalID
    this.elem ++= elements.map(d => d.name -> d).toMap
    this.attr ++= attributeLists.map(d => d.name -> d).toMap
    this.ent ++= entities.map { d =>
      val name: String = d match {
        case ParsedEntityDecl(name, _) => name
        case ParameterEntityDecl(name, _) => name
        case UnparsedEntityDecl(name, _, _) => name
      }
      name -> d
    }.toMap
    this.decls =
      elements.reverse ++
        attributeLists.reverse ++
        entities.reverse ++
        DtdBuilder.this.notations.reverse ++
        parameterReferences.reverse

    override val notations: Seq[NotationDecl] = DtdBuilder.this.notations.reverse
    override val unparsedEntities: Seq[EntityDecl] = DtdBuilder.this.unparsedEntities.reverse
  }


  def elementDecl(name: String, model: String): Unit = {
    flushAttributes()
    elements ::= ElemDecl(name, ElementContentModel.parseContentModel(model))
  }

  // The type will be one of the strings "CDATA", "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES",
  // a parenthesized token group with the separator "|" and all whitespace removed,
  // or the word "NOTATION" followed by a space followed by a parenthesized token group with all whitespace removed.
  def attributeDecl(
    eName: String,
    aName: String,
    `type`: String,
    mode: String,
    value: String
  ): Unit = {
    if (!elementName.contains(eName)) {
      flushAttributes()
      elementName = Some(eName)
    }

    val attribute: AttrDecl = AttrDecl(
      aName,
      `type`,
      mode match {
        case "#REQUIRED" => REQUIRED
        case "#IMPLIED" => IMPLIED
        case "#FIXED" => DEFAULT(fixed = true, value)
        case _ => DEFAULT(fixed = false, value)
      }
    )

    attributes ::= attribute
  }

  // General entities are reported with their regular names,
  // parameter entities have '%' prepended to their names,
  // and the external DTD subset has the pseudo-entity name "[dtd]".
  def startEntity(name: String): Unit = {
    flushAttributes()
    if (name.startsWith("%")) parameterReferences ::= PEReference(name.tail.trim)
  }

  def endEntity(name: String): Unit = {
  }

  def notationDecl(
    name: String,
    publicId: String,
    systemId: String
  ): Unit = {
    flushAttributes()
    notations ::= NotationDecl(name, DtdBuilder.mkExternalID(publicId, systemId))
  }

  def unparsedEntityDecl(
    name: String,
    publicId: String,
    systemId: String,
    notationName: String
  ): Unit = {
    flushAttributes()
    val unparsedEntity: UnparsedEntityDecl =
      UnparsedEntityDecl(name, DtdBuilder.mkExternalID(publicId, systemId), notationName)
    entities ::= unparsedEntity
    unparsedEntities ::= unparsedEntity
  }

  def internalEntityDecl(
    name: String,
    value: String
  ): Unit = {
    flushAttributes()
    entityDecl(name, IntDef(value))
  }

  def externalEntityDecl(
    name: String,
    publicId: String,
    systemId: String
  ): Unit = {
    flushAttributes()
    entityDecl(name, ExtDef(DtdBuilder.mkExternalID(publicId, systemId)))
  }

  private def entityDecl(
    name: String,
    entityDef: EntityDef
  ): Unit = {
    val entity: EntityDecl =
      if (name.startsWith("%")) ParameterEntityDecl(name.tail.trim, entityDef)
      else ParsedEntityDecl(name, entityDef)
    entities ::= entity
  }

  // DTD class currently does not provide for capturing processing instructions
  def processingInstruction(target: String, data: String): Unit = ()

  // DTD class currently does not provide for capturing comments
  def comment(commentText: String): Unit = ()
}

// Note: this is private to avoid it becoming a part of binary compatibility checks
private[parsing] object DtdBuilder {
  def apply(
    name: String,
    publicId: String,
    systemId: String
  ): DtdBuilder = new DtdBuilder(
    name,
    mkExternalID(publicId, systemId)
  )

  private def mkExternalID(publicId: String, systemId: String): ExternalID =
    if (publicId != null) PublicID(publicId, systemId)
    else if (systemId != null) SystemID(systemId)
    else NoExternalID
}
