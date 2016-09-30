package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }
import org.scalacheck.Prop.AnyOperators

object QNodeSpec extends CheckProperties("QNode")
    with NodeGen {

  property("unapplySeq") = {
    Prop.forAll { n: Node =>
      val res = QNode.unapplySeq(n)
      res ?= Some((n.scope.getURI(n.prefix), n.label, n.attributes, n.child))
    }
  }
}
