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

trait ProcInstrGen extends XmlNameGen {

  val validTarget: Gen[String] =
    genXmlName.suchThat(_.toLowerCase != "xml")

  val validProcText: Gen[String] =
    genXmlName.suchThat(!_.contains("?>")): Gen[String]

  val genProcInstr: Gen[ProcInstr] = for {
    target <- validTarget
    proctext <- validProcText
  } yield {
    new ProcInstr(target, proctext)
  }

  def invalidProcInstr: Gen[ProcInstr] = for {
    badTarget <- Gen.oneOf(invalidXmlName, Gen.oneOf("XML", "xml", "Xml"))
    badProcText <- Gen.const("?>")
    goodTarget <- validTarget
    goodProcText <- validProcText
    procinstr <- Gen.oneOf(
      Gen.delay(new ProcInstr(goodTarget, badProcText)),
      Gen.delay(new ProcInstr(badTarget, goodProcText)),
      Gen.delay(new ProcInstr(badTarget, badProcText))
    )
  } yield {
    procinstr
  }

  implicit val arbProcInstr = Arbitrary {
    genProcInstr
  }
}
