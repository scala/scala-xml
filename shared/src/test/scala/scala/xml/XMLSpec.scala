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
import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }

object XMLSpec extends CheckProperties("XML")
    with NodeGen
    with dtd.DocTypeGen {

  def genWriter: Gen[java.io.Writer] =
    Gen.const(
      new java.io.OutputStreamWriter(
        new java.io.ByteArrayOutputStream,
        "UTF-8"
      )
    )

  implicit val arbWriter = Arbitrary {
    genWriter
  }

  property("write") = {
    Prop.forAll { (w: java.io.Writer, node: Node, xmlDecl: Boolean, doctype: dtd.DocType) =>
      XML.write(w, node, "UTF-8", xmlDecl, doctype)
      Prop.passed
    }
  }
}
