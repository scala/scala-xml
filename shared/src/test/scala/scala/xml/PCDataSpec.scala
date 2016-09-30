package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }
import org.scalacheck.Prop.AnyOperators

object PCDataSpec extends CheckProperties("PCData")
    with PCDataGen {

  property("text") = {
    Prop.forAll { d: PCData =>
      d.text ?= d.data
    }
  }

  property("toString") = {
    Prop.forAll { d: PCData =>
      d.toString ?= s"<![CDATA[${d.data}]]>"
    }
  }

  property("unapply") = {
    Prop.forAll { d: PCData =>
      PCData.unapply(d) ?= Some(d.data)
    }
  }
}
