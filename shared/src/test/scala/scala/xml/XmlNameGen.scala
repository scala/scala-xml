/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait XmlNameGen {

  def isLetter = Utility.isAlpha _
  def isLetterOrDigit = Utility.isAlphaDigit _

  def validPrefix: Gen[Char] = Gen.oneOf(Gen.alphaChar, Gen.oneOf("_".toSeq))
  def validChar: Gen[Char] =
    Gen.oneOf(Gen.alphaNumChar, Gen.oneOf(".-_:".toSeq))

  val genXmlName: Gen[String] = for {
    arbPrefix <- validPrefix
    arbName <- Gen.listOf(validChar)
  } yield {
    arbPrefix.toString + arbName.mkString
  }

  def invalidPrefix: Gen[Char] =
    Arbitrary.arbitrary[Char].suchThat(!Utility.isNameStart(_))

  def invalidChar: Gen[Char] =
    Arbitrary.arbitrary[Char].suchThat(!Utility.isNameChar(_))

  def invalidXmlName: Gen[String] = for {
    goodPrefix <- validPrefix
    goodName <- Gen.listOf(validChar)
    badPrefix <- invalidPrefix
    badName <- Gen.listOf(invalidChar)
    name <- Gen.oneOf(
      goodPrefix.toString + badName.mkString,
      badPrefix.toString + goodName.mkString,
      badPrefix.toString + badName.mkString
    )
  } yield {
    name
  }
}
