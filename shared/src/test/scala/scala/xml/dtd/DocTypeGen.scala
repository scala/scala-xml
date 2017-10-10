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

trait DocTypeGen extends DeclGen
    with ExternalIDGen
    with XmlNameGen {

  def genDocType(sz: Int): Gen[DocType] = for {
    name <- genXmlName: Gen[String]
    extID <- Arbitrary.arbitrary[ExternalID]
    n <- Gen.choose(0, scala.math.sqrt(sz / 2).toInt)
    intSubset <- Gen.listOfN(n, Arbitrary.arbitrary[dtd.Decl])
  } yield {
    new DocType(name, extID, intSubset)
  }

  implicit val arbDocType = Arbitrary {
    Gen.sized(sz => genDocType(sz))
  }
}
