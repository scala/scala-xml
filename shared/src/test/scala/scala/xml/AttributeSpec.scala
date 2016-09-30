package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators
import org.scalacheck.Prop.BooleanOperators

object AttributeSpec extends PropertiesFor("Attribute")
    with AttributeGen
    with NamespaceBindingGen
    with NodeGen
    with MetaDataGen {

  property("canEqual(this)") = {
    Prop.forAll { a: Attribute =>
      a.canEqual(a)
    }
  }

  property("apply") = {
    Prop.forAll { (a: Attribute, s: String) =>
      (!s.isEmpty && Utility.isNameStart(s(0))) ==>
        (a(s) != s)
    }
  }

  property("apply(key)") = {
    Prop.forAll { a: Attribute =>
      Prop.iff[Attribute](a, {
        case p: PrefixedAttribute =>
          Prop.passed
        case u: UnprefixedAttribute if u.key ne null =>
          u(u.key) ?= u.value
      })
    }
  }

  property("apply(uri, namespace, key)") = {
    Prop.forAll { a: Attribute =>
      Prop.iff[Attribute](a, {
        case p: PrefixedAttribute =>
          val res = p(p.key, NamespaceBinding(p.pre, p.key, TopScope), p.key)
          res ?= p.value
        case u: UnprefixedAttribute =>
          Prop.passed
      })
    }
  }

  property("isPrefixed") = {
    Prop.forAll { a: Attribute =>
      Prop.iff[Attribute](a, {
        case p: PrefixedAttribute =>
          a.isPrefixed ?= true
        case u: UnprefixedAttribute =>
          a.isPrefixed ?= false
      })
    }
  }

  property("length") = {
    Prop.forAll { a: Attribute =>
      a.length >= 0
    }
  }

  property("size") = {
    Prop.forAll { a: Attribute =>
      a.size >= 0
    }
  }

  property("toString") = {
    Prop.forAll { a: Attribute =>
      val str = a.toString
      Prop.iff[Attribute](a, {
        case a: PrefixedAttribute if a.hasNext =>
          str ?= s""" ${a.pre}:${a.key}="${a.value}"${a.next}"""
        case a: UnprefixedAttribute if a.hasNext =>
          str ?= s""" ${a.key}="${a.value}"${a.next}"""
        case a: PrefixedAttribute =>
          str ?= s""" ${a.pre}:${a.key}="${a.value}""""
        case a: UnprefixedAttribute =>
          str ?= s""" ${a.key}="${a.value}""""
      })
    }
  }

  property("unapply") = {
    Prop.forAll { a: Attribute =>
      Attribute.unapply(a) ?= Some((a.key, a.value, a.next))
    }
  }

  property("remove(key)") = {
    Prop.forAll { a: Attribute =>
      Prop.iff[Attribute](a, {
        case a: PrefixedAttribute =>
          a.remove(a.key) ?= a.copy(a.next.remove(a.key)) // FIXME: ???
        case a: UnprefixedAttribute =>
          a.remove(a.key) ?= a.next
      })
    }
  }

  property("remove") = {
    Prop.forAll { (a: Attribute, s: String) =>
      s != a.key ==>
        (a.remove(s) ?= a.copy(a.next.remove(s)))
    }
  }

  property("getNamespace") = {
    Prop.forAll { (a: Attribute, n: Node) =>
      a.getNamespace(n) ?= n.getNamespace(a.pre)
    }
  }

  property("wellformed") = {
    Prop.forAll { (a: Attribute, scope: NamespaceBinding) =>
      Prop.iff[Attribute](a, {
        case a: PrefixedAttribute =>
          val res = a.wellformed(scope)
          res ?= ((null eq a.next(scope.getURI(a.pre), scope, a.key)) && a.next.wellformed(scope))
        case a: UnprefixedAttribute =>
          val res = a.wellformed(scope)
          res ?= ((null eq a.next(null, scope, a.key)) && a.next.wellformed(scope))
      })
    }
  }
}
