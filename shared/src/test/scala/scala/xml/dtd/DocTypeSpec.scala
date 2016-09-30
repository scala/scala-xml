package scala.xml
package dtd

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators
import org.scalacheck.Prop.BooleanOperators

object DocTypeSpec extends PropertiesFor("dtd.DocType")
    with DocTypeGen
    with ExternalIDGen
    with DeclGen {

  property("new(name)") = {
    Prop.forAll { (name: String) =>
      Prop.atLeastOne(
        Utility.isName(name) ==> {
          DocType(name)
          Prop.passed
        },
        !Utility.isName(name) ==>
          Prop.throws(classOf[IllegalArgumentException]) {
            DocType(name)
        }
      )
    }
  }

  property("new(name, extId, intSubset)") = {
    Prop.forAll { (name: String, extID: ExternalID, intSubset: dtd.Decl) =>
      Prop.atLeastOne(
        Utility.isName(name) ==> {
          new DocType(name, extID, Seq(intSubset))
          Prop.passed
        },
        !Utility.isName(name) ==>
          Prop.throws(classOf[IllegalArgumentException]) {
            new DocType(name, extID, Seq(intSubset))
          }
      )
    }
  }

  property("new(name, extId, emptyInt)") = {
    Prop.forAll { (name: String, extID: ExternalID) =>
      Prop.atLeastOne(
        Utility.isName(name) ==> {
          new DocType(name, extID, Seq.empty[dtd.Decl])
          Prop.passed
        },
        !Utility.isName(name) ==>
          Prop.throws(classOf[IllegalArgumentException]) {
            new DocType(name, extID, Seq.empty[dtd.Decl])
          }
      )
    }
  }

  property("toString") = {
    Prop.forAll { d: DocType =>
      val str = d.toString
      val intSubset = d.intSubset.mkString
      Prop.atLeastOne(
        str ?= s"<!DOCTYPE ${d.name} ${d.extID}>",
        str ?= s"<!DOCTYPE ${d.name} [$intSubset]>",
        str ?= s"<!DOCTYPE ${d.name} ${d.extID}[$intSubset]>"
      )
    }
  }
}
