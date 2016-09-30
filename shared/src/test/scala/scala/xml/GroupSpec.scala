package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object GroupSpec extends PropertiesFor("Group")
    with NodeGen
    with ElemGen
    with GroupGen {

  property("attributes") =
    Prop.forAll { g: Group =>
      Prop.throws(classOf[UnsupportedOperationException]) {
        g.attributes
      }
    }

  property("child") =
    Prop.forAll { g: Group =>
      Prop.throws(classOf[UnsupportedOperationException]) {
        g.child
      }
    }

  property("label") =
    Prop.forAll { g: Group =>
      Prop.throws(classOf[UnsupportedOperationException]) {
        g.label
      }
    }

  property("namespace") =
    Prop.forAll { g: Group =>
      Prop.throws(classOf[UnsupportedOperationException]) {
        g.namespace
      }
    }

  property("descendant") =
    Prop.forAll { g: Group =>
      Prop.throws(classOf[UnsupportedOperationException]) {
        g.descendant
      }
    }

  property("descendant_or_self") =
    Prop.forAll { g: Group =>
      Prop.throws(classOf[UnsupportedOperationException]) {
        g.descendant_or_self
      }
    }

  property("empty.\\.throws[Exception]") = {
    val g = Group(List())
    (g \ "a") ?= NodeSeq.Empty
  }

  // FIXME: UnsupportedOperationException: class Group does not support method 'child'
  // property("\\.throws[Exception]") = {
  //   Prop.forAll { g: Group =>
  //     (g \ "a")
  //     Prop.passed // FIXME: Check that the result is actually correct.
  //   }
  // }

  property("theSeq") = {
    Prop.forAll { g: Group =>
      g.theSeq ?= g.nodes
    }
  }
}
