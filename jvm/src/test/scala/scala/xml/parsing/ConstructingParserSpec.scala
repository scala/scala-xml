/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

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
