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

trait TextGen extends Utf8StringGen {

  val genText: Gen[Text] = for {
    s <- genUtf8String: Gen[String]
  } yield {
    new Text(s)
  }

  implicit val arbText = Arbitrary {
    genText
  }
}
