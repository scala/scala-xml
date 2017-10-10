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

trait ContentModelGen extends impl.RegExpGen {

  // val genDFAContentModel: Gen[ContentModel] = for {
  //   r <- Arbitrary.arbitrary[_regexpT]
  //   d <- Gen.oneOf(MIXED(r), ELEMENTS(r))
  // } yield {
  //   d
  // }

  val genContentModel: Gen[ContentModel] =
    Gen.oneOf(
      Gen.const(PCDATA), Gen.const(EMPTY), Gen.const(ANY)
      // genDFAContentModel
    )

  implicit val arbContentModel = Arbitrary {
    genContentModel
  }
}
