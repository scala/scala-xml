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
package dtd

import Utility.sbToString

/**
 * XML declarations
 * 
 *  - [[scala.xml.dtd.AttListDecl]] — Attribute list declaration (ATTLIST)
 *  - [[scala.xml.dtd.AttrDecl]] — Attribute declaration
 *  - [[scala.xml.dtd.ElemDecl]] — Element declaration (ELEMENT)
 *  - [[scala.xml.dtd.ParameterEntityDecl]] — Parameter entity list (ENTITY %)
 *  - [[scala.xml.dtd.ParsedEntityDecl]] — Parsed general entity list (ENTITY)
 *  - [[scala.xml.dtd.PEReference]] — Parsed entity reference
 *  - [[scala.xml.dtd.UnparsedEntityDecl]] — Unparsed entity list (ENTITY NDATA)
 */
sealed abstract class Decl

sealed abstract class MarkupDecl extends Decl {
  override def toString: String = sbToString(buildString)
  def buildString(sb: StringBuilder): StringBuilder
}

/**
 * an element declaration
 */
case class ElemDecl(name: String, contentModel: ContentModel)
  extends MarkupDecl {
  override def buildString(sb: StringBuilder): StringBuilder = {
    sb.append(s"<!ELEMENT $name ")
    ContentModel.buildString(contentModel, sb)
    sb.append('>')
  }
}

case class AttListDecl(name: String, attrs: List[AttrDecl])
  extends MarkupDecl {
  override def buildString(sb: StringBuilder): StringBuilder =
    sb.append(s"<!ATTLIST $name\n${attrs.mkString("\n")}>")
}

/**
 * an attribute declaration. at this point, the tpe is a string. Future
 *  versions might provide a way to access the attribute types more
 *  directly.
 */
case class AttrDecl(name: String, tpe: String, default: DefaultDecl) {
  override def toString: String = sbToString(buildString)

  def buildString(sb: StringBuilder): StringBuilder = {
    sb.append(s"  $name $tpe ")
    default.buildString(sb)
  }
}

/** an entity declaration */
sealed abstract class EntityDecl extends MarkupDecl

/** a parsed general entity declaration */
case class ParsedEntityDecl(name: String, entdef: EntityDef) extends EntityDecl {
  override def buildString(sb: StringBuilder): StringBuilder = {
    sb.append(s"<!ENTITY $name ")
    entdef.buildString(sb).append('>')
  }
}

/** a parameter entity declaration */
case class ParameterEntityDecl(name: String, entdef: EntityDef) extends EntityDecl {
  override def buildString(sb: StringBuilder): StringBuilder = {
    sb.append(s"<!ENTITY % $name ")
    entdef.buildString(sb).append('>')
  }
}

/** an unparsed entity declaration */
case class UnparsedEntityDecl(name: String, extID: ExternalID, notation: String) extends EntityDecl {
  override def buildString(sb: StringBuilder): StringBuilder = {
    sb.append(s"<!ENTITY $name ")
    extID.buildString(sb).append(s" NDATA $notation>")
  }
}

/** a notation declaration */
case class NotationDecl(name: String, extID: ExternalID) extends MarkupDecl {
  override def buildString(sb: StringBuilder): StringBuilder = {
    sb.append(s"<!NOTATION $name ")
    extID.buildString(sb).append('>')
  }
}

sealed abstract class EntityDef {
  def buildString(sb: StringBuilder): StringBuilder
}

case class IntDef(value: String) extends EntityDef {
  private def validateValue(): Unit = {
    var tmp: String = value
    var ix: Int = tmp.indexOf('%')
    while (ix != -1) {
      val iz: Int = tmp.indexOf(';', ix)
      if (iz == -1 && iz == ix + 1)
        throw new IllegalArgumentException("no % allowed in entity value, except for parameter-entity-references")
      else {
        val n: String = tmp.substring(ix, iz)

        if (!Utility.isName(n))
          throw new IllegalArgumentException(s"""internal entity def: "$n" must be an XML Name""")

        tmp = tmp.substring(iz + 1, tmp.length)
        ix = tmp.indexOf('%')
      }
    }
  }
  validateValue()

  override def buildString(sb: StringBuilder): StringBuilder =
    Utility.appendQuoted(value, sb)

}

case class ExtDef(extID: ExternalID) extends EntityDef {
  override def buildString(sb: StringBuilder): StringBuilder =
    extID.buildString(sb)
}

/** a parsed entity reference */
case class PEReference(ent: String) extends MarkupDecl {
  if (!Utility.isName(ent))
    throw new IllegalArgumentException("ent must be an XML Name")

  override def buildString(sb: StringBuilder): StringBuilder =
    sb.append(s"%$ent;")
}

// default declarations for attributes

sealed abstract class DefaultDecl {
  def toString: String
  def buildString(sb: StringBuilder): StringBuilder
}

case object REQUIRED extends DefaultDecl {
  override def toString: String = "#REQUIRED"
  override def buildString(sb: StringBuilder): StringBuilder = sb.append("#REQUIRED")
}

case object IMPLIED extends DefaultDecl {
  override def toString: String = "#IMPLIED"
  override def buildString(sb: StringBuilder): StringBuilder = sb.append("#IMPLIED")
}

case class DEFAULT(fixed: Boolean, attValue: String) extends DefaultDecl {
  override def toString: String = sbToString(buildString)
  override def buildString(sb: StringBuilder): StringBuilder = {
    if (fixed) sb.append("#FIXED ")
    Utility.appendEscapedQuoted(attValue, sb)
  }
}
