package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ArbitraryGroup {

  def genGroup(sz: Int): Gen[Group]

  implicit val arbGroup: Arbitrary[Group]
}
