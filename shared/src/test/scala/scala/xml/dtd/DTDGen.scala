/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml
package dtd

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait DTDGen extends DeclGen
    with ExternalIDGen {

  def genDTD(sz: Int): Gen[DTD] = for {
    extID <- Gen.oneOf(
      Gen.const(null),
      Arbitrary.arbitrary[ExternalID]
    )
    n <- Gen.choose(0, scala.math.sqrt(sz / 2).toInt)
    intSubset <- Gen.listOfN(n, Arbitrary.arbitrary[dtd.Decl])
    notationDecls <- Gen.listOfN(n, Arbitrary.arbitrary[NotationDecl])
  } yield {
    new DTD {
      externalID = extID
      decls = intSubset
      override def notations = notationDecls
    }
  }

  implicit def arbDTD = Arbitrary {
    Gen.sized(sz => genDTD(sz))
  }
}
