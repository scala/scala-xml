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

object ElemSpec extends PropertiesFor("Elem")
    with NodeSeqGen
    with MetaDataGen
    with NamespaceBindingGen {

  property("new") = {
    Prop.forAll { (prefix: String, label: String, attribs: MetaData,
                   scope: NamespaceBinding, minimizeEmpty: Boolean,
                   children: NodeSeq) =>
      Prop.atLeastOne(
        !prefix.isEmpty ==> {
          new Elem(
            prefix,
            label,
            attribs,
            scope,
            minimizeEmpty,
            children.theSeq: _*
          )
          Prop.passed
        },
        prefix.isEmpty ==>
          Prop.throws(classOf[IllegalArgumentException]) {
            new Elem(
              prefix,
              label,
              attribs,
              scope,
              minimizeEmpty,
              children.theSeq: _*
            )
          }
      )
    }
  }

  property("unapplySeq(Elem)") = {
    Prop.forAll { e: Elem =>
      val opt = Elem.unapplySeq(e)
      opt ?= Some((e.prefix, e.label, e.attributes, e.scope, e.child))
    }
  }

  property("unapplySeq(Node)") = {
    Prop.forAll { n: Node =>
      val opt = Elem.unapplySeq(n)
      Prop.iff[Node](n, {
        case _: SpecialNode | _: Group =>
          opt ?= None
        case _ =>
          opt ?= Some((n.prefix, n.label, n.attributes, n.scope, n.child.toSeq))
      })
    }
  }
}
