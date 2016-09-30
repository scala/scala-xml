package scala.xml
package dtd

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object DTDSpec extends PropertiesFor("dtd.DTD")
    with DTDGen {

  property("toString") = {
    Prop.forAll { d: DTD =>
      val str = d.toString
      val decls = d.decls.mkString("\n")
      Prop.atLeastOne(
        str ?= s"DTD [\n${d.externalID}${decls}\n]",
        str ?= s"DTD [\n${decls}\n]"
      )
    }
  }
}
