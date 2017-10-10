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

trait PCDataGen extends Utf8StringGen {

  val genPCData: Gen[PCData] = for {
    s <- genUtf8String: Gen[String]
  } yield {
    PCData(s)
  }

  implicit val arbPCData = Arbitrary {
    genPCData
  }
}
