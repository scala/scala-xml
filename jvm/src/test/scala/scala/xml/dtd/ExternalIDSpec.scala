package scala.xml
package dtd

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object ExternalIDSpec extends PropertiesFor("dtd.ExternalID")
    with ExternalIDGen {

  property("PublicID.throws[Exception]") = {
    Prop.forAll(genNonPubIdStr) { s: String =>
      Prop.throws(classOf[IllegalArgumentException]) {
        PublicID(s, s)
      }
    }
  }

  property("SystemID.throws[Exception]") = {
    Prop.forAll(genNonSysIdStr) { s: String =>
      Prop.throws(classOf[IllegalArgumentException]) {
        SystemID(s)
      }
    }
  }

  property("SystemID(null).throws[Exception]") = {
    Prop.throws(classOf[NullPointerException]) {
      SystemID(null)
    }
  }

  property("label") = {
    Prop.forAll { p: PublicID =>
      p.label ?= "#PI"
    }
  }

  property("attribute") = {
    Prop.forAll { p: PublicID =>
      p.attribute ?= Node.NoAttributes
    }
  }

  property("child") = {
    Prop.forAll { p: PublicID =>
      p.child ?= Nil
    }
  }

  property("toString") = Prop.forAll { e: ExternalID =>
    val str = e.toString
    Prop.atLeastOne(
      str ?= "",
      str ?= s"""SYSTEM '${e.systemId}'""",
      str ?= s"""SYSTEM "${e.systemId}"""",
      str ?= s"""PUBLIC '${e.publicId}'""",
      str ?= s"""PUBLIC "${e.publicId}"""",
      str ?= s"""PUBLIC '${e.publicId}' '${e.systemId}'""",
      str ?= s"""PUBLIC "${e.publicId}" "${e.systemId}"""",
      str ?= s"""PUBLIC '${e.publicId}' "${e.systemId}"""",
      str ?= s"""PUBLIC "${e.publicId}" '${e.systemId}'"""
    )
  }
}
