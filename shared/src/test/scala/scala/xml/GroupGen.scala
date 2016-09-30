package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait GroupGen extends ArbitraryGroup
    with ArbitraryNode {

  def genGroup(sz: Int): Gen[Group] = for {
    n <- Gen.choose(0, scala.math.sqrt(sz / 2).toInt)
    nodes <- Gen.listOfN(n, genNode(n))
  } yield {
    Group(nodes)
  }

  implicit val arbGroup = Arbitrary {
    Gen.sized(sz => genGroup(sz))
  }
}
