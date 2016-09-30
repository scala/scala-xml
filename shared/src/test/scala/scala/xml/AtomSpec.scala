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
