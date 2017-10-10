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
