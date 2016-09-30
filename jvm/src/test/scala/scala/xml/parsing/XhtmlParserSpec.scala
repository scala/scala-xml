package scala.xml
package parsing

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }

object XhtmlParserSpec extends PropertiesFor("parsing.XhtmlParser")
    with XhtmlParserGen {

  property("initialize") = {
    Prop.forAll { parser: XhtmlParser =>
      parser.initialize eq parser
    }
  }

  property("prolog") = {
    Prop.forAll { parser: XhtmlParser =>
      parser.prolog
      Prop.passed
    }
  }

  property("document") = {
    Prop.forAll { parser: XhtmlParser =>
      parser.document
      Prop.passed
    }
  }
}
