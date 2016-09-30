package scala.xml
package parsing

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }

object ConstructingParserSpec extends PropertiesFor("parsing.ConstructingParser")
    with ConstructingParserGen {

  property("initialize") = {
    Prop.forAll { parser: ConstructingParser =>
      parser.initialize eq parser
    }
  }

  property("prolog") = {
    Prop.forAll { parser: ConstructingParser =>
      parser.prolog
      Prop.passed
    }
  }

  property("document") = {
    Prop.forAll { parser: ConstructingParser =>
      parser.document
      Prop.passed
    }
  }
}
