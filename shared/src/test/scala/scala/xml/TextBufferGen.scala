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

trait TextBufferGen extends ArbitraryTextBuffer
    with TextGen {

  implicit val arbTextBuffer = Arbitrary {
    genTextBuffer
  }

  val genTextBuffer: Gen[TextBuffer] = for {
    str <- Arbitrary.arbitrary[String]
  } yield {
    TextBuffer.fromString(str)
  }
}
