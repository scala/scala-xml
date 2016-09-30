package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators
import org.scalacheck.Prop.BooleanOperators

object MetaDataSpec extends PropertiesFor("MetaData")
    with ElemGen
    with GroupGen
    with NodeGen
    with MetaDataGen {

  property("canEqual(this)") = {
    Prop.forAll { m: MetaData =>
      m.canEqual(m)
    }
  }

  property("apply") = {
    Prop.forAll { (m: MetaData, s: String) =>
      ((!s.isEmpty && Utility.isNameStart(s(0))) ==> (m(s) != s))
    }
  }

  property("apply(key)") = {
    Prop.forAll { m: MetaData =>
      Prop.iff[MetaData](m, {
        case p: PrefixedAttribute => Prop.passed
        case u: UnprefixedAttribute if u.key ne null => u(u.key) ?= u.value
        case Null => Prop.passed
      })
    }
  }

  property("apply(uri, namespace, key)") = {
    Prop.forAll { m: MetaData =>
      Prop.iff[MetaData](m, {
        case p: PrefixedAttribute =>
          p(p.key, NamespaceBinding(p.pre, p.key, TopScope), p.key) ?= p.value
        case u: UnprefixedAttribute => Prop.passed
        case Null => Prop.passed
      })
    }
  }

  property("isPrefixed") = {
    Prop.forAll { m: MetaData =>
      Prop.atLeastOne(
        m.isPrefixed ?= true,
        m.isPrefixed ?= false
      )
    }
  }

  property("iterator") = {
    Prop.forAll { m: MetaData =>
      Prop.iff[MetaData](m, {
        case a: Attribute if a.value eq null =>
          a.iterator.sameElements(a.next.iterator)
        case a: Attribute =>
          a.iterator.sameElements(Iterator.single(a) ++ a.next.iterator)
        case Null =>
          m.iterator ?= Iterator.empty
      })
    }
  }

  property("length") = {
    Prop.forAll { m: MetaData =>
      m.length >= 0
    }
  }

  property("size") = {
    Prop.forAll { m: MetaData =>
      m.size >= 0
    }
  }

  property("toString") = {
    Prop.forAll { m: MetaData =>
      val str = m.toString
      Prop.iff[MetaData](m, {
        case m: MetaData if m.value eq null =>
          str ?= ""
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
}
