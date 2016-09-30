package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait TextGen extends Utf8StringGen {

  val genText: Gen[Text] = for {
    s <- genUtf8String: Gen[String]
  } yield {
    new Text(s)
  }

  implicit val arbText = Arbitrary {
    genText
  }
}
