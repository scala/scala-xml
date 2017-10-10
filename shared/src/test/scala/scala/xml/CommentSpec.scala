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

object CommentSpec extends PropertiesFor("Comment")
    with CommentGen {

  property("new(--).throws[Exception]") = {
    Prop.throws(classOf[IllegalArgumentException]) {
      new Comment("--")
    }
  }

  property("child") = {
    Prop.forAll { n: Comment =>
      n.child ?= Nil
    }
  }

  property("text") = {
    Prop.forAll { n: Comment =>
      n.text ?= ""
    }
  }

  property("toString") = {
    Prop.forAll { n: Comment =>
      val str = n.toString
      Prop.atLeastOne(
        str ?= "<!---->",
        str.startsWith("<!--") && str.endsWith("-->")
      )
    }
  }
}
