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

trait ElemGen extends NamespaceBindingGen
    with ArbitraryNode
    with MetaDataGen
    with XmlNameGen {

  val genStringOrNull: Gen[String] =
    Gen.oneOf(
      genXmlName: Gen[String],
      Gen.const(null)
    )

  def genElem(sz: Int): Gen[Elem] = for {
    prefix <- genStringOrNull
    label <- genXmlName: Gen[String]
    attribs <- Arbitrary.arbitrary[MetaData]
    scope <- Arbitrary.arbitrary[NamespaceBinding]
    minimizeEmpty <- Arbitrary.arbitrary[Boolean]
    n <- Gen.choose(0, scala.math.sqrt(sz / 2).toInt)
    children <- Gen.listOfN(n, genNode(n))
  } yield {
    Elem(
      prefix,
      label,
      attribs,
      scope,
      minimizeEmpty,
      children: _*
    )
  }

  def invalidElem(sz: Int): Gen[Elem] = for {
    prefix <- Gen.const("")
    label <- Arbitrary.arbitrary[String]
    attribs <- Arbitrary.arbitrary[MetaData]
    scope <- Arbitrary.arbitrary[NamespaceBinding]
    minimizeEmpty <- Arbitrary.arbitrary[Boolean]
    n <- Gen.choose(0, scala.math.sqrt(sz / 2).toInt)
    children <- Gen.listOfN(n, genNode(n))
  } yield {
    Elem(
      prefix,
      label,
      attribs,
      scope,
      minimizeEmpty,
      children: _*
    )
  }

  implicit val arbElem = Arbitrary {
    Gen.sized(sz => genElem(sz))
  }
}
