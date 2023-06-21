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

package scala.xml.parsing

import scala.annotation.tailrec
import scala.xml.dtd

// Note: this is private to avoid it becoming a part of binary compatibility checks.

// The content model will consist of the string "EMPTY", the string "ANY", or a parenthesised group,
// optionally followed by an occurrence indicator.
// The model will be normalized so that all parameter entities are fully resolved and all whitespace is removed,
// and will include the enclosing parentheses.
// Other normalization (such as removing redundant parentheses or simplifying occurrence indicators)
// is at the discretion of the parser.

// elementdecl         ::=       '<!ELEMENT' S Name S contentspec S? '>'
// contentspec         ::=       'EMPTY' | 'ANY' | Mixed | children
// children            ::=       (choice | seq) ('?' | '*' | '+')?
// cp                  ::=       (Name | choice | seq) ('?' | '*' | '+')?
// choice              ::=       '(' S? cp ( S? '|' S? cp )+ S? ')'
// seq                 ::=       '(' S? cp ( S? ',' S? cp )* S? ')'
// Mixed               ::=       '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*'
//                             | '(' S? '#PCDATA' S? ')'
private[parsing] object ElementContentModel {
  def parseContentModel(model: String): dtd.ContentModel = ContentSpec.parse(model) match {
    case ContentSpec.Empty => dtd.EMPTY
    case ContentSpec.Any => dtd.ANY
    case ContentSpec.PCData => dtd.PCDATA
    case ContentSpec.Children(elements, occurrence) => dtd.ELEMENTS(convertOccurrence(elements, occurrence))
    case ContentSpec.Mixed(elements) =>
      val result: List[dtd.ContentModel.RegExp] =
        dtd.ContentModel.Letter(dtd.ContentModel.ElemName(ContentSpec.PCData.value)) +:
        elements.map(convertElements)
      // TODO scala.xml.dtd.impl.Alt.apply() insists on there being al least two alternatives,
      // which causes an exception in MIXED.toString() when there is only one alternative besides #PCDATA.
      // I think this is a bug.
      dtd.MIXED(dtd.ContentModel.Alt(result: _*))
  }

  private def convertElements(elements: Elements): dtd.ContentModel.RegExp = {
    def convertCp(cp: Cp): dtd.ContentModel.RegExp = convertOccurrence(cp.elements, cp.occurrence)
    elements match {
      case Elements.Element(name) => dtd.ContentModel.Letter(dtd.ContentModel.ElemName(name))
      case Elements.Choice(children) => dtd.ContentModel.Alt(children.map(convertCp): _*)
      case Elements.Sequence(children) => dtd.ContentModel.Sequ(children.map(convertCp): _*)
    }
  }

  private def convertOccurrence(elements: Elements, occurrence: Occurrence): dtd.ContentModel.RegExp = {
    val result: dtd.ContentModel.RegExp = convertElements(elements)
    occurrence match {
      case Occurrence.Once => result
      case Occurrence.RepeatOptional => dtd.ContentModel.Star(result)
      case Occurrence.OnceOptional => dtd.ContentModel.Star(result) // TODO fidelity lost!
      case Occurrence.Repeat => dtd.ContentModel.Star(result) // TODO fidelity lost!
    }
  }

  sealed trait ContentSpec
  object ContentSpec {
    sealed trait Simple extends ContentSpec {
      final override def toString: String = value
      val value: String
    }
    case object Empty extends Simple {
      override val value: String = "EMPTY"
    }
    case object Any extends Simple {
      override val value: String = "ANY"
    }
    case object PCData extends ContentSpec {
      override def toString: String = s"($value)"
      val value: String = "#PCDATA"
    }
    final case class Mixed(elements: List[Elements.Element]) extends ContentSpec {
      override def toString: String = {
        val names: String = elements.mkString("|")
        s"(${PCData.value}|$names)*"
      }
    }
    final case class Children(elements: Elements.Many, occurrence: Occurrence) extends ContentSpec {
      override def toString: String = s"$elements$occurrence"
    }
    object Children {
      def parse(string: String, occurrence: Occurrence): Children =
        Children(Elements.Many.parse(string), occurrence)
    }
    def parse(model: String): ContentSpec = model match {
      case Empty.value => Empty
      case Any.value => Any
      case model =>
        val (parenthesized: String, occurrence: Occurrence) = Occurrence.parse(model)
        require(isParenthesized(parenthesized))
        val string: String = removeParentheses(parenthesized)
        if (occurrence == Occurrence.Once && string == PCData.value) PCData else if (occurrence == Occurrence.RepeatOptional) {
          val choice: List[String] = Elements.Choice.split(string)
          if (choice.length > 1 && choice.head == PCData.value) Mixed(choice.tail.map(Elements.Element))
          else Children.parse(string, occurrence)
        } else Children.parse(string, occurrence)
    }
  }

  sealed trait Elements
  object Elements {
    final case class Element(name: String) extends Elements {
      override def toString: String = name
    }
    sealed abstract class ManyCompanion(val separator: Char) {
      final def split(string: String): List[String] = ElementContentModel.split(string, separator)
    }
    sealed abstract class Many(children: List[Cp]) extends Elements {
      final override def toString: String = children.map(_.toString).mkString("(", companion.separator.toString, ")")
      def companion: ManyCompanion
    }
    object Choice extends ManyCompanion(separator = '|')
    final case class Choice(children: List[Cp]) extends Many(children) {
      override def companion: ManyCompanion = Choice
    }
    object Sequence extends ManyCompanion(separator = ',')
    final case class Sequence(children: List[Cp]) extends Many(children) {
      override def companion: ManyCompanion = Sequence
    }
    object Many {
      def parse(string: String): Many = {
        val choice: List[String] = Choice.split(string)
        if (choice.length > 1) Choice(choice.map(Cp.parse))
        else Sequence(Sequence.split(string).map(Cp.parse))
      }
    }
    def parse(string: String): Elements =
      if (!isParenthesized(string)) Element(string)
      else Many.parse(removeParentheses(string))
  }

  final case class Cp(elements: Elements, occurrence: Occurrence) {
    override def toString: String = s"$elements$occurrence"
  }
  object Cp {
    def parse(string: String): Cp = {
      val (maybeParenthesized: String, occurrence: Occurrence) = Occurrence.parse(string)
      Cp(Elements.parse(maybeParenthesized), occurrence)
    }
  }

  sealed class Occurrence
  object Occurrence {
    case object Once extends Occurrence {
      override def toString: String = ""
    }
    sealed trait Signed extends Occurrence {
      final override def toString: String = sign
      def sign: String
    }
    case object OnceOptional extends Signed {
      override def sign: String = "?"
    }
    case object Repeat extends Signed {
      override def sign: String = "+"
    }
    case object RepeatOptional extends Signed {
      override def sign: String = "*"
    }
    def parse(string: String): (String, Occurrence) =
      if (string.endsWith(OnceOptional.sign)) (string.init, OnceOptional) else
      if (string.endsWith(RepeatOptional.sign)) (string.init, RepeatOptional) else
      if (string.endsWith(Repeat.sign)) (string.init, Repeat) else
        (string, Once)
  }

  private def isParenthesized(string: String): Boolean = {
    @tailrec
    def isParenthesized(level: Int, tail: String): Boolean = {
      val current: Char = tail.head
      val nextTail: String = tail.tail
      val nextLevel: Int = if (current == '(') level + 1 else if (current == ')') level - 1 else level
      if (nextTail.isEmpty) nextLevel == 0 else if (nextLevel == 0) false else isParenthesized(nextLevel, nextTail)
    }

    string.startsWith("(") && isParenthesized(0, string)
  }

  @tailrec
  private def removeParentheses(string: String): String =
    if (!isParenthesized(string)) string
    else removeParentheses(string.tail.init)

  // split at the top level of parentheses
  private def split(string: String, separator: Char): List[String] = {
    @tailrec
    def split(
      result: List[String],
      level: Int,
      init: String,
      tail: String
    ): List[String] = if (tail.isEmpty) if (init.isEmpty) result else result :+ init else {
      val current: Char = tail.head
      val nextTail: String = tail.tail
      if (level == 0 && current == separator) split(
        result :+ init,
        level,
        "",
        nextTail
      ) else split(
        result,
        if (current == '(') level + 1 else if (current == ')') level - 1 else level,
        init :+ current,
        nextTail
      )
    }

    split(
      List.empty,
      0,
      "",
      string
    )
  }
}
