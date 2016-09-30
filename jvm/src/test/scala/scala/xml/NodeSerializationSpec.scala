/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2018, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }
import org.scalacheck.Prop.AnyOperators
import org.scalacheck.Prop.BooleanOperators

object NodeSerializationSpec extends CheckProperties("NodeSerialization")
    with NodeGen {

  property("serialization") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n =>
          JavaByteSerialization.roundTrip(n) ?= n
      })
    }
  }
}
