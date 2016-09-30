package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait NodeBufferGen extends ArbitraryNodeBuffer
    with NodeGen {

  implicit val arbNodeBuffer = Arbitrary {
    genNodeBuffer
  }

  val genNodeBuffer: Gen[NodeBuffer] =
    Gen.delay(new NodeBuffer)
}
