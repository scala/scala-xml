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

trait XhtmlParserGen {

  val genXhtmlParser: Gen[XhtmlParser] = for {
    // FIXME: Doesn't generate valid XML strings
    xml <- Arbitrary.arbitrary[String]
    preserveWS <- Arbitrary.arbitrary[Boolean]
  } yield {
    new XhtmlParser(scala.io.Source.fromString(xml)) {
      override def reportSyntaxError(str: String) = {}
    }
  }

  implicit val arbXhtmlParser = Arbitrary {
    genXhtmlParser
  }
}
