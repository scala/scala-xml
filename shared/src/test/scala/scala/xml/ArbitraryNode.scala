package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ArbitraryNode {

  def genNode(sz: Int): Gen[Node]

  implicit val arbNode: Arbitrary[Node]
}
