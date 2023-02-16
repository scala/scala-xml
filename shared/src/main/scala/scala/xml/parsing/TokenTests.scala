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

import scala.collection.Seq

/**
 * Helper functions for parsing XML fragments
 */
trait TokenTests {

  /**
   * {{{
   *  (#x20 | #x9 | #xD | #xA)
   *  }}}
   */
  final def isSpace(ch: Char): Boolean = ch match {
    case '\u0009' | '\u000A' | '\u000D' | '\u0020' => true
    case _                                         => false
  }
  /**
   * {{{
   *  (#x20 | #x9 | #xD | #xA)+
   *  }}}
   */
  final def isSpace(cs: Seq[Char]): Boolean = cs.nonEmpty && (cs forall isSpace)

  /** These are 99% sure to be redundant but refactoring on the safe side. */
  def isAlpha(c: Char): Boolean = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
  def isAlphaDigit(c: Char): Boolean = isAlpha(c) || (c >= '0' && c <= '9')

  /**
   * {{{
   *  NameChar ::= Letter | Digit | '.' | '-' | '_' | ':' | #xB7
   *             | CombiningChar | Extender
   *  }}}
   *  See [4] and [4a] of Appendix B of XML 1.0 specification.
   */
  def isNameChar(ch: Char): Boolean = {
    import java.lang.Character._
    // The constants represent groups Mc, Me, Mn, Lm, and Nd.

    isNameStart(ch) || (getType(ch).toByte match {
      case COMBINING_SPACING_MARK |
        ENCLOSING_MARK | NON_SPACING_MARK |
        MODIFIER_LETTER | DECIMAL_DIGIT_NUMBER => true
      case _ => ".-:·" contains ch
    })
  }

  /**
   * {{{
   *  NameStart ::= ( Letter | '_' | ':' )
   *  }}}
   *  where Letter means in one of the Unicode general
   *  categories `{ Ll, Lu, Lo, Lt, Nl }`.
   *
   *  We do not allow a name to start with `:`.
   *  See [4] and Appendix B of XML 1.0 specification
   */
  def isNameStart(ch: Char): Boolean = {
    import java.lang.Character._

    getType(ch).toByte match {
      case LOWERCASE_LETTER |
        UPPERCASE_LETTER | OTHER_LETTER |
        TITLECASE_LETTER | LETTER_NUMBER => true
      case _ => ":_".contains(ch)
    }
  }

  /**
   * {{{
   *  Name ::= ( Letter | '_' ) (NameChar)*
   *  }}}
   *  See [5] of XML 1.0 specification.
   */
  def isName(s: String): Boolean =
    s.nonEmpty && isNameStart(s.head) && (s.tail forall isNameChar)

  def isPubIDChar(ch: Char): Boolean =
    isAlphaDigit(ch) || (isSpace(ch) && ch != '\u0009') ||
      ("""-\()+,./:=?;!*#@$_%""" contains ch)

  /**
   * Returns `true` if the encoding name is a valid IANA encoding.
   * This method does not verify that there is a decoder available
   * for this encoding, only that the characters are valid for an
   * IANA encoding name.
   *
   * @param ianaEncoding The IANA encoding name.
   */
  def isValidIANAEncoding(ianaEncoding: Seq[Char]): Boolean = {
    def charOK(c: Char): Boolean = isAlphaDigit(c) || ("._-" contains c)

    ianaEncoding.nonEmpty && isAlpha(ianaEncoding.head) &&
      (ianaEncoding.tail forall charOK)
  }

  def checkSysID(s: String): Boolean = List('"', '\'') exists (c => !(s contains c))
  def checkPubID(s: String): Boolean = s forall isPubIDChar
}
