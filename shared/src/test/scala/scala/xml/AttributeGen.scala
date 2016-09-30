package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait AttributeGen extends TextGen
    with ArbitraryMetaData
    with XmlNameGen {

  def genAttribute: Gen[Attribute] =
    Gen.oneOf(
      Arbitrary.arbitrary[PrefixedAttribute],
      Arbitrary.arbitrary[UnprefixedAttribute]
    )

  val genPrefixedAttribute: Gen[PrefixedAttribute] = for {
    prefix <- genXmlName: Gen[String]
    key <- genXmlName: Gen[String]
    value <- Arbitrary.arbitrary[Text]
    next <- Gen.delay(genMetaData)
  } yield {
    new PrefixedAttribute(prefix, key, value, next)
  }

  val genUnprefixedAttribute: Gen[UnprefixedAttribute] = for {
    key <- genXmlName: Gen[String]
    value <- Arbitrary.arbitrary[Text]
    next <- Gen.delay(genMetaData)
  } yield {
    new UnprefixedAttribute(key, value, next)
  }

  implicit val arbAttribute: Arbitrary[Attribute] = Arbitrary {
    genAttribute
  }

  implicit val arbPrefixedAttribute: Arbitrary[PrefixedAttribute] = Arbitrary {
    genPrefixedAttribute
  }

  implicit val arbUnprefixedAttribute: Arbitrary[UnprefixedAttribute] = Arbitrary {
    genUnprefixedAttribute
  }
}
