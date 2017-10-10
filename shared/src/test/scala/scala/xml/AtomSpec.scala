/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object AtomSpec extends PropertiesFor("Atom")
    with AtomGen {

  property("new(null).throws[Exception]") = {
    Prop.throws(classOf[IllegalArgumentException]) {
      new Atom(null)
    }
  }

  property("data") = {
    Prop.forAll { a: Atom[String] =>
      a.data ne null
    }
  }

  property("text") = {
    Prop.forAll { a: Atom[String] =>
      a.text ?= s"${a.data}"
    }
  }
}
