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

trait NodeGen extends ArbitraryNode
    with ElemGen
    // FIXME: UnsupportedOperationException: class Group does not support method
    // with GroupGen
    with AtomGen
    with CommentGen
    with EntityRefGen
    with ProcInstrGen {

  def genNode(sz: Int): Gen[Node] =
    Gen.oneOf(
      genAtom,
      genComment,
      Gen.delay(genElem(scala.math.sqrt(sz / 2).toInt)),
      genEntityRef,
      // Gen.delay(genGroup(scala.math.sqrt(sz / 2).toInt)), // FIXME: See above.
      genProcInstr
    )

  implicit val arbNode = Arbitrary {
    Gen.sized(sz => genNode(sz))
  }
}
