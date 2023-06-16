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

import scala.collection.Seq
import scala.xml.dtd.impl._
import scala.xml.Utility.sbToString
import PartialFunction._

/*
@deprecated("Avoidance", since="2.10")
trait ContentModelLaundry extends WordExp
object ContentModelLaundry extends ContentModelLaundry {
}
*/

object ContentModel extends WordExp {

  override type _labelT = ElemName
  override type _regexpT = RegExp

  @deprecated("Avoidance", since="2.10")
  trait Translator extends WordBerrySethi
  object Translator extends Translator {
    override val lang: ContentModel.this.type = ContentModel.this
  }

  case class ElemName(name: String) extends Label {
    override def toString: String = s"""ElemName("$name")"""
  }

  def isMixed(cm: ContentModel): Boolean = cond(cm) { case _: MIXED => true }
  def containsText(cm: ContentModel): Boolean = (cm == PCDATA) || isMixed(cm)

  def getLabels(r: RegExp): Set[String] = {
    def traverse(r: RegExp): Set[String] = r match { // !!! check for match translation problem
      case Letter(ElemName(name)) => Set(name)
      case Star(x@_)              => traverse(x) // bug if x@_*
      case Sequ(xs@_*)            => Set(xs.flatMap(traverse): _*)
      case Alt(xs@_*)             => Set(xs.flatMap(traverse): _*)
    }

    traverse(r)
  }

  def buildString(r: RegExp): String = sbToString(buildString(r, _))

  /* precond: rs.length >= 1 */
  private def buildString(rs: Seq[RegExp], sb: StringBuilder, sep: Char): Unit = {
    buildString(rs.head, sb)
    for (z <- rs.tail) {
      sb.append(sep)
      buildString(z, sb)
    }
  }

  def buildString(c: ContentModel, sb: StringBuilder): StringBuilder = c match {
    case ANY                    => sb.append("ANY")
    case EMPTY                  => sb.append("EMPTY")
    case PCDATA                 => sb.append("(#PCDATA)")
    case ELEMENTS(_) | MIXED(_) => c.buildString(sb)
  }

  def buildString(r: RegExp, sb: StringBuilder): StringBuilder =
    r match { // !!! check for match translation problem
      case Eps =>
        sb
      case Sequ(rs@_*) =>
        sb.append('('); buildString(rs, sb, ','); sb.append(')')
      case Alt(rs@_*) =>
        sb.append('('); buildString(rs, sb, '|'); sb.append(')')
      case Star(r: RegExp) =>
        sb.append('('); buildString(r, sb); sb.append(")*")
      case Letter(ElemName(name)) =>
        sb.append(name)
    }
}

sealed abstract class ContentModel {
  override def toString: String = sbToString(buildString)
  def buildString(sb: StringBuilder): StringBuilder
}

import ContentModel.RegExp

case object PCDATA extends ContentModel {
  override def buildString(sb: StringBuilder): StringBuilder = sb.append("(#PCDATA)")
}
case object EMPTY extends ContentModel {
  override def buildString(sb: StringBuilder): StringBuilder = sb.append("EMPTY")
}
case object ANY extends ContentModel {
  override def buildString(sb: StringBuilder): StringBuilder = sb.append("ANY")
}
sealed abstract class DFAContentModel extends ContentModel {
  import ContentModel.{ElemName, Translator}
  def r: RegExp

  lazy val dfa: DetWordAutom[ElemName] = {
    val nfa: NondetWordAutom[ElemName] = Translator.automatonFrom(r, 1)
    new SubsetConstruction(nfa).determinize
  }
}

case class MIXED(override val r: RegExp) extends DFAContentModel {
  import ContentModel.Alt

  override def buildString(sb: StringBuilder): StringBuilder = {
    val newAlt: Alt = r match { case Alt(rs@_*) => Alt(rs.drop(1): _*) }

    sb.append("(#PCDATA|")
    ContentModel.buildString(newAlt: RegExp, sb)
    sb.append(")*")
  }
}

case class ELEMENTS(override val r: RegExp) extends DFAContentModel {
  override def buildString(sb: StringBuilder): StringBuilder =
    ContentModel.buildString(r, sb)
}
