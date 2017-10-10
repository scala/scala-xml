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

trait AtomGen extends PCDataGen
    with TextGen
    with UnparsedGen {

  val genAtom: Gen[Atom[String]] =
    Gen.oneOf(genPCData, genText, genUnparsed)

  implicit val arbAtom = Arbitrary {
    genAtom
  }
}
