package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ArbitraryElem {

  def genElem(sz: Int): Gen[Elem]

  implicit val arbElem: Arbitrary[Elem]
}
