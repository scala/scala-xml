package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait PCDataGen extends Utf8StringGen {

  val genPCData: Gen[PCData] = for {
    s <- genUtf8String: Gen[String]
  } yield {
    PCData(s)
  }

  implicit val arbPCData = Arbitrary {
    genPCData
  }
}
