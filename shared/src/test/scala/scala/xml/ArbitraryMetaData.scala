package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ArbitraryMetaData {

  val genMetaData: Gen[MetaData]

  implicit val arbMetaData: Arbitrary[MetaData]
}
