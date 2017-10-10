/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }

object PrettyPrinterSpec extends CheckProperties("PrettyPrinter")
    with PrettyPrinterGen
    with NodeGen {

  property("format") = {
    Prop.forAll { (pp: PrettyPrinter, n: Node) =>
      pp.format(n).length >= 0
    }
  }

  property("formatNodes") = {
    Prop.forAll { (pp: PrettyPrinter, ns: Seq[Node]) =>
      pp.formatNodes(ns).length >= 0
    }
  }
}
