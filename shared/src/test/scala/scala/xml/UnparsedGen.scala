package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait UnparsedGen extends Utf8StringGen {

  val genUnparsed: Gen[Unparsed] = for {
    s <- genUtf8String: Gen[String]
  } yield {
    new Unparsed(s)
  }

  implicit val arbUnparsed = Arbitrary {
    genUnparsed
  }
}
