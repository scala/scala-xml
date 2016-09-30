package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait TextBufferGen extends ArbitraryTextBuffer
    with TextGen {

  implicit val arbTextBuffer = Arbitrary {
    genTextBuffer
  }

  val genTextBuffer: Gen[TextBuffer] = for {
    str <- Arbitrary.arbitrary[String]
  } yield {
    TextBuffer.fromString(str)
  }
}
