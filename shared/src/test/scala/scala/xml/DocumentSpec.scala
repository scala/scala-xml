package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object DocumentSpec extends PropertiesFor("Document")
    with DocumentGen {

  property("baseURI") = {
    Prop.forAll { d: Document =>
      d.baseURI eq null
    }
  }

  property("children") = {
    Prop.forAll { d: Document =>
      Prop.atLeastOne(
        d.children eq null,
        d.isInstanceOf[Seq[Node]]
      )
    }
  }

  property("docElem") = {
    Prop.forAll { d: Document =>
      Prop.atLeastOne(
        d.theSeq eq null,
        d.theSeq.isInstanceOf[Node]
      )
    }
  }

  property("dtd") = {
    Prop.forAll { d: Document =>
      Prop.atLeastOne(
        d.dtd eq null,
        d.dtd.isInstanceOf[dtd.DTD]
      )
    }
  }

  property("encoding") = {
    Prop.forAll { d: Document =>
      d.encoding eq null
    }
  }

  property("standAlone") = {
    Prop.forAll { d: Document =>
      d.standAlone eq null
    }
  }

  property("version") = {
    Prop.forAll { d: Document =>
      d.version eq null
    }
  }

  property("allDeclarationsProcessed") = {
    Prop.forAll { d: Document =>
      d.allDeclarationsProcessed ?= false
    }
  }

  property("theSeq") = {
    Prop.forAll { d: Document =>
      d.theSeq eq d.docElem
    }
  }

  property("canEqual") = {
    Prop.forAll { d: Document =>
      d.canEqual(d)
    }
  }
}
