/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object NodeBufferSpec extends PropertiesFor("NodeBuffer")
    with NodeBufferGen
    with NodeGen {

  implicit val arbAny = Arbitrary {
    genAny
  }

  val genAny: Gen[Any] =
    Gen.oneOf(
      Gen.const(null),
      Gen.const((): Unit),
      Arbitrary.arbitrary[Node],
      Arbitrary.arbitrary[List[Node]].map(_.toIterator),
      Arbitrary.arbitrary[List[Node]].map(_.toIterable),
      Arbitrary.arbitrary[Array[Node]]
    )

  property("&+") = {
    Prop.forAll { (nb: NodeBuffer, a: Any) =>
      nb&+(a)
      Prop.passed
    }
  }

  property("toString") = {
    Prop.forAll { n: NodeBuffer =>
      Prop.all(
        n.toString ?= "NodeBuffer()",
        (n &+ "").toString.startsWith("NodeBuffer")
      )
    }
  }
}
