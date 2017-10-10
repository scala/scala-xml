/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml
package parsing

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait ConstructingParserGen extends XmlStringGen {

  val genConstructingParser: Gen[ConstructingParser] = for {
    xml <- Gen.oneOf(
      genXmlString: Gen[String],
      Arbitrary.arbitrary[String]
    )
    preserveWS <- Arbitrary.arbitrary[Boolean]
  } yield {
    // ConstructingParser.fromSource(scala.io.Source.fromString(xml), preserveWS)
    new ConstructingParser(scala.io.Source.fromString(xml), preserveWS) {
      override def reportSyntaxError(str: String) = {}
    }
  }

  implicit val arbConstructingParser = Arbitrary {
    genConstructingParser
  }
}
