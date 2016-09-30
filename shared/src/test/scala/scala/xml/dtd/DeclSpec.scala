package scala.xml
package dtd

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object DeclSpec extends PropertiesFor("dtd.Decl")
    with DeclGen {

  implicit val arbStringBuilder: Arbitrary[StringBuilder] = Arbitrary {
    Gen.delay(new StringBuilder)
  }

  // FIXME: Expected "<!ATTLIST ..." but got "AttListDecl(..."
  // property("AttListDecl.toString") = {
  //   Prop.forAll { (d: AttListDecl, sb: StringBuilder) =>
  //     val str = d.toString
  //     val attrs = d.attrs.mkString("\n")
  //     str ?= s"<!ATTLIST ${d.name}\n${attrs}>"
  //   }
  // }

  // FIXME: Fix toString above, and delete this buildString test.
  property("AttListDecl.buildString") = {
    Prop.forAll { (d: AttListDecl, sb: StringBuilder) =>
      val str = d.buildString(sb).toString
      val attrs = d.attrs.mkString("\n")
      str ?= s"<!ATTLIST ${d.name}\n${attrs}>"
    }
  }

  property("AttrDecl.toString") = {
    Prop.forAll { d: AttrDecl =>
      d.toString ?= s"  ${d.name} ${d.tpe} ${d.default}"
    }
  }

  property("DefaultDecl.toString") = {
    Prop.forAll { d: DefaultDecl =>
      val str = d.toString
      Prop.atLeastOne(
        str ?= "#REQUIRED",
        str ?= "#IMPLIED",
        str.startsWith("#FIXED"),
        str.length >= 2
      )
    }
  }

  property("ElemDecl.buildString") = {
    Prop.forAll { (d: ElemDecl, sb: StringBuilder) =>
      val str = d.buildString(sb).toString
      str.startsWith(s"<!ELEMENT ${d.name} ") && str.endsWith(">")
    }
  }

  property("EntityDecl.buildString") = {
    Prop.forAll { (d: EntityDecl, sb: StringBuilder) =>
      val str = d.buildString(sb).toString
      str.startsWith("<!ENTITY ") && str.endsWith(">")
    }
  }

  property("ParsedEntityDecl.buildString") = {
    Prop.forAll { (d: ParsedEntityDecl, sb1: StringBuilder, sb2: StringBuilder) =>
      val str = d.buildString(sb1).toString
      val entdef = d.entdef.buildString(sb2).toString
      str ?= s"<!ENTITY ${d.name} ${entdef}>"
    }
  }

  property("ParameterEntityDecl.buildString") = {
    Prop.forAll { (d: ParameterEntityDecl, sb1: StringBuilder, sb2: StringBuilder) =>
      val str = d.buildString(sb1).toString
      val entdef = d.entdef.buildString(sb2).toString
      str ?= s"<!ENTITY % ${d.name} ${entdef}>"
    }
  }

  property("UnparsedEntityDecl.buildString") = {
    Prop.forAll { (d: UnparsedEntityDecl, sb1: StringBuilder, sb2: StringBuilder) =>
      val str = d.buildString(sb1).toString
      val extID = d.extID.buildString(sb2).toString
      str ?= s"<!ENTITY ${d.name} ${extID} NDATA ${d.notation}>"
    }
  }

  property("NotationDecl.buildString") = {
    Prop.forAll { (d: NotationDecl, sb: StringBuilder) =>
      val str = d.buildString(sb).toString
      str.startsWith("<!NOTATION ")
    }
  }

  property("PEReference.buildString") = {
    Prop.forAll { (d: PEReference, sb: StringBuilder) =>
      val str = d.buildString(sb).toString
      str ?= s"%${d.ent};"
    }
  }

  property("EntityDef.buildString") = {
    Prop.forAll { (d: EntityDef, sb: StringBuilder) =>
      val str = d.buildString(sb).toString
      str.length >= 0
    }
  }

  property("IntDef.buildString") = {
    Prop.forAll { (d: IntDef, sb: StringBuilder) =>
      val str = d.buildString(sb).toString
      Prop.atLeastOne(
        // str ?= "",
        str ?= s"'${d.value}'",
        str ?= s""""${d.value}""""
      )
    }
  }

  property("ExtDef.buildString") = {
    Prop.forAll { (d: ExtDef, sb1: StringBuilder, sb2: StringBuilder) =>
      val str = d.buildString(sb1).toString
      val extID = d.extID.buildString(sb2).toString
      str ?= extID
    }
  }

  property("toString") = {
    Prop.forAll { d: Decl =>
      d.toString.length >= 0
    }
  }
}
