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

object NodeSpec extends CheckProperties("Node")
    with NodeGen {

  property("canEqual(this)") = {
    Prop.forAll { n: Node =>
      n.canEqual(n)
    }
  }

  property("canEqual") = {
    Prop.forAll { (n1: Node, n2: Node) =>
      val b = n1.canEqual(n2)
      Prop.atLeastOne(
        b ?= true,
        b ?= false
      )
    }
  }

  property("prefix") = {
    Prop.forAll { n: Text =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.prefix eq null
        case n @ EntityRef(_) => n.prefix eq null
        case n: PCData => n.prefix eq null
        case n @ ProcInstr(_, _) => n.prefix eq null
        case n: Text => n.prefix eq null
        case n: Unparsed => n.prefix eq null
        case e: Elem =>
          Prop.atLeastOne(
            // e.prefix eq null, // FIXME: NullPointerException: null
            e.prefix eq null,
            e.prefix != ""
          )
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.label
          }
        case a: Atom[_] => n.prefix eq null
      })
    }
  }

  property("label") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.label ?= "#REM"
        case n @ EntityRef(_) => n.label ?= "#ENTITY"
        case n: PCData => n.label ?= "#PCDATA"
        case n @ ProcInstr(_, _) => n.label ?= "#PI"
        case n: Text => n.label ?= "#PCDATA"
        case n: Unparsed => n.label ?= "#PCDATA"
        case n: Elem => n.label.length >= 0
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.label
          }
        case n: Atom[_] => n.label ?= "#PCDATA"
      })
    }
  }

  property("isAtom") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n: Atom[_] => n.isAtom ?= true
        case _ => n.isAtom ?= false
      })
    }
  }

  property("doCollectNamespaces") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.doCollectNamespaces ?= false
        case n @ EntityRef(_) => n.doCollectNamespaces ?= false
        case n @ ProcInstr(_, _) => n.doCollectNamespaces ?= false
        case n: PCData => n.doCollectNamespaces ?= false
        case n: Unparsed => n.doCollectNamespaces ?= false
        case n: Text => n.doCollectNamespaces ?= false
        case n: Atom[_] => n.doCollectNamespaces ?= false
        case n: Elem => n.doCollectNamespaces ?= true
        case n @ Group(_) => n.doCollectNamespaces ?= true
      })
    }
  }

  property("doTransform") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.doTransform ?= false
        case n @ EntityRef(_) => n.doTransform ?= false
        case n @ ProcInstr(_, _) => n.doTransform ?= false
        case n: PCData => n.doTransform ?= false
        case n: Unparsed => n.doTransform ?= false
        case n: Text => n.doTransform ?= false
        case n: Atom[_] => n.doTransform ?= false
        case n: Elem => n.doTransform ?= true
        case n @ Group(_) => n.doTransform ?= true
      })
    }
  }

  property("scope") = {
    Prop.forAll { n: Node =>
      n.scope ne null
    }
  }

  property("namespace") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.namespace
          }
        case _ =>
          n.namespace ?= n.getNamespace(n.prefix)
      })
    }
  }

  // FIXME: NoSuchElementException thrown by Null.apply
  // property("attribute(\"\")") = {
  //   Prop.forAll { n: Node =>
  //     Prop.throws(classOf[IllegalArgumentException]) {
  //       n.attribute("")
  //     }
  //   }
  // }

  property("attribute") = {
    Prop.forAll { (n: Node, s: String) =>
      Prop.iff[Node](n, {
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.attribute(s)
          }
        case _ =>
          Prop.atLeastOne(
            s.isEmpty ==>
              Prop.throws(classOf[NoSuchElementException]) {
                n.attribute(s)
              } ,
            ((!s.isEmpty) && Utility.isNameStart(s.head)) ==>
              Prop.throws(classOf[NoSuchElementException]) {
                n.attribute(s)
              },
            ((!s.isEmpty) && !Utility.isNameStart(s.head)) ==>
              Prop.throws(classOf[IllegalArgumentException]) {
                n.attribute(s)
              }
          )
      })
    }
  }

  property("attributes") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.attributes ?= Null
        case n @ EntityRef(_) => n.attributes ?= Null
        case n: PCData => n.attributes ?= Null
        case n @ ProcInstr(_, _) => n.attributes ?= Null
        case n: Text => n.attributes ?= Null
        case n: Unparsed => n.attributes ?= Null
        case n: Elem => n.attributes.length >= 0
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.attributes
          }
      })
    }
  }

  property("child") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.child ?= Nil
        case n @ EntityRef(_) => n.child ?= Nil
        case n: PCData => n.child ?= Nil
        case n @ ProcInstr(_, _) => n.child ?= Nil
        case n: Text => n.child ?= Nil
        case n: Unparsed => n.child ?= Nil
        case n: Elem => n.child.length >= 0
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.child
          }
      })
    }
  }

  property("nonEmptyChildren") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.nonEmptyChildren ?= Nil
        case n @ EntityRef(_) => n.nonEmptyChildren ?= Nil
        case n: PCData => n.nonEmptyChildren ?= Nil
        case n @ ProcInstr(_, _) => n.nonEmptyChildren ?= Nil
        case n: Text => n.nonEmptyChildren ?= Nil
        case n: Unparsed => n.nonEmptyChildren ?= Nil
        case n: Elem => n.nonEmptyChildren.length >= 0
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.nonEmptyChildren
          }
      })
    }
  }

  property("descendant") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.descendant ?= Nil
        case n @ EntityRef(_) => n.descendant ?= Nil
        case n: PCData => n.descendant ?= Nil
        case n @ ProcInstr(_, _) => n.descendant ?= Nil
        case n: Text => n.descendant ?= Nil
        case n: Unparsed => n.descendant ?= Nil
        case n: Elem => n.descendant.length >= 0
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.descendant
          }
      })
    }
  }

  property("descendant_or_self") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(_) => n.descendant_or_self ?= List(n)
        case n @ EntityRef(_) => n.descendant_or_self ?= List(n)
        case n: PCData => n.descendant_or_self ?= List(n)
        case n @ ProcInstr(_, _) => n.descendant_or_self ?= List(n)
        case n: Text => n.descendant_or_self ?= List(n)
        case n: Unparsed => n.descendant_or_self ?= List(n)
        case n: Elem => n.descendant_or_self ?= n :: n.descendant
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.descendant_or_self
          }
      })
    }
  }

  property("theSeq") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Group(nodes) =>
            n.theSeq ?= nodes
        case _ =>
          n.theSeq ?= n :: Nil
      })
    }
  }

  property("xmlType") = {
    Prop.forAll { n: Node =>
      n.xmlType eq null
    }
  }

  property("nameToString") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            n.namespace
          }
        case _ => {
          val str = n.nameToString(new StringBuilder).toString
          Prop.atLeastOne(
            str ?= s"${n.prefix}:${n.label}",
            str ?= s"${n.label}"
          )
        }
      })
    }
  }

  property("text") = {
    Prop.forAll { n: Node =>
      n.text.length >= 0
    }
  }

  property("toString") = {
    Prop.forAll { n: Node =>
      n.toString.length >= 0
    }
  }

  property("match") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Comment(commentText: String) =>
          commentText ?= n.commentText
        case Elem(null, _: String, _: MetaData, _: NamespaceBinding, _ @ _*) =>
          Prop.passed
        case Elem(_: String, _: String, _: MetaData, _: NamespaceBinding, _ @ _*) =>
          Prop.passed
        case n @ EntityRef(entityName) =>
          entityName ?= n.entityName
        case n @ Group(nodes) =>
          Prop.passed
        case n @ PCData(data) =>
          PCData.unapply(n) ?= Some(data)
        case n @ ProcInstr(_: String, _: String) =>
          Prop.passed
        case n @ Text(_: String) =>
          Prop.passed
        case n @ Unparsed(_: String) =>
          Prop.passed
      })
    }
  }

  property("unapplySeq") = {
    Prop.forAll { n: Node =>
      Prop.iff[Node](n, {
        case n @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            Node.unapplySeq(n)
          }
        case _ =>
          Node.unapplySeq(n) ?= Some((n.label, n.attributes, n.child.toSeq))
      })
    }
  }
}
