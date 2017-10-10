/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators
import org.scalacheck.Prop.BooleanOperators

object UtilitySpec extends PropertiesFor("Utility")
    with NodeGen {

  val escape = Map(
    '<' -> "lt",
    '>' -> "gt",
    '&' -> "amp",
    '"' -> "quot" // ,
    // '\'' -> "apos"
  )

  property("escape") = {
    Prop.forAll { (text: String) =>
      val sb = new StringBuilder
      val res = Utility.escape(text)
      val exp = text.iterator.filter { c =>
        (c >= ' ' || "\n\r\t".contains(c))
      }.foldLeft(sb) { (sb, c) =>
        escape.get(c).map { str =>
          sb ++= s"&$str;"
        }.getOrElse {
          sb += c
        }
      }
      res ?= exp.toString
    }
  }

  property("escape(\"\\u0000\")") = {
    Utility.escape("\u0000") ?= ""
  }

  val unescape = escape.map {
    case (c, str) => str -> c.toString
  }

  property("unescape") = {
    Prop.forAll { (text: String) =>
      val sb = new StringBuilder
      val res = Utility.unescape(text, sb)
      Prop.atLeastOne(
        unescape.contains(text) ==> {
          val exp = unescape.get(text).getOrElse {
            text
          }
          res.toString ?= exp
        },
        !unescape.contains(text) ==>
          (res eq null)
      )
    }
  }

  property("serialize") = {
    Prop.forAll { x: Node =>
      Utility.serialize(x).toString ?= x.toString
    }
  }
}
