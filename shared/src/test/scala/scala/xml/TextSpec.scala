package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }
import org.scalacheck.Prop.AnyOperators

object TextSpec extends CheckProperties("Text")
    with TextGen {

  property("text") = {
    Prop.forAll { t: Text =>
      t.text ?= s"${t.data}"
    }
  }

  property("unapply") = {
    Prop.forAll { t: Text =>
      Text.unapply(t) ?= Some(t.data)
    }
  }
}
