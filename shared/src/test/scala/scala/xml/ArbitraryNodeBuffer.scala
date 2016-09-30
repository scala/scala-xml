package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ArbitraryNodeBuffer {

  val genNodeBuffer: Gen[NodeBuffer]

  implicit val arbNodeBuffer: Arbitrary[NodeBuffer]
}
