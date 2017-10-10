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
import org.scalacheck.Prop.AnyOperators

object ProcInstrSpec extends CheckProperties("ProcInstr")
    with ProcInstrGen {

  property("label") = {
    Prop.forAll { p: ProcInstr =>
      p.label == "#PI"
    }
  }

  property("text") = {
    Prop.forAll { p: ProcInstr =>
      p.text ?= ""
    }
  }

  property("toString") = {
    Prop.forAll { (p: ProcInstr) =>
      val str = p.toString
      Prop.atLeastOne(
        str ?= s"<?${p.target}?>",
        str ?= s"<?${p.target} ${p.proctext}?>"
      )
    }
  }

  property("new(name, \"?>\").throws[Exception]") = {
    Prop.throws(classOf[IllegalArgumentException]) {
      new ProcInstr("name", "?>")
    }
  }

  property("new(<>, text).throws[Exception]") = {
    Prop.throws(classOf[IllegalArgumentException]) {
      new ProcInstr("<>", "text")
    }
  }

  property("new(xml, text).throws[Exception]") = {
    Prop.throws(classOf[IllegalArgumentException]) {
      new ProcInstr("xml", "text")
    }
  }
}
