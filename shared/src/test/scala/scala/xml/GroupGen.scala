/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

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
