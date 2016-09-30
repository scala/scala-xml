package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait MetaDataGen extends AttributeGen
    with ArbitraryMetaData {

  val genMetaData: Gen[MetaData] = for {
    metaData <- Gen.oneOf(Gen.const(Null), genAttribute)
  } yield {
    metaData
  }

  implicit val arbMetaData = Arbitrary {
    genMetaData
  }
}
