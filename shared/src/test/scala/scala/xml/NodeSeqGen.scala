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

trait NodeSeqGen extends NodeGen {

  implicit val arbNodeSeq = Arbitrary {
    Gen.sized(sz => genNodeSeq(sz))
  }

  def genNodeSeq(sz: Int): Gen[NodeSeq] = for {
    n <- Gen.choose(0, scala.math.sqrt(sz / 2).toInt)
    empty <- Gen.const(NodeSeq.Empty)
    s <- Gen.listOfN(n, genNode(n))
    nodeSeq <- Gen.oneOf(empty, NodeSeq.fromSeq(s.toSeq))
  } yield {
    nodeSeq
  }
}
