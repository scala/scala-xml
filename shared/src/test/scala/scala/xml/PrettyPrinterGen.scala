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

trait PrettyPrinterGen {

  implicit val arbPrettyPrinter = Arbitrary {
    genPrettyPrinter
  }

  val genPrettyPrinter: Gen[PrettyPrinter] = for {
    width <- Gen.posNum[Int]
    step <- Gen.posNum[Int]
  } yield {
    new PrettyPrinter(width, step)
  }
}
