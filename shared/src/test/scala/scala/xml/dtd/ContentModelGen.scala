package scala.xml
package dtd

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ContentModelGen extends impl.RegExpGen {

  // val genDFAContentModel: Gen[ContentModel] = for {
  //   r <- Arbitrary.arbitrary[_regexpT]
  //   d <- Gen.oneOf(MIXED(r), ELEMENTS(r))
  // } yield {
  //   d
  // }

  val genContentModel: Gen[ContentModel] =
    Gen.oneOf(
      Gen.const(PCDATA), Gen.const(EMPTY), Gen.const(ANY)
      // genDFAContentModel
    )

  implicit val arbContentModel = Arbitrary {
    genContentModel
  }
}
