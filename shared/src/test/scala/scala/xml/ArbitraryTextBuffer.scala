package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ArbitraryTextBuffer {

  val genTextBuffer: Gen[TextBuffer]

  implicit val arbTextBuffer: Arbitrary[TextBuffer]
}
