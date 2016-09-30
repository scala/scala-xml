package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object NamespaceBindingSpec extends PropertiesFor("NamespaceBinding")
    with NamespaceBindingGen {

  property("canEqual(this)") = {
    Prop.forAll { n: NamespaceBinding =>
      n.canEqual(n)
    }
  }

  property("getPrefix") = {
    Prop.forAll { (n: NamespaceBinding, s: String) =>
      n.getPrefix(s) != s
    }
  }

  property("getPrefix(uri)") = {
    Prop.forAll { n: NamespaceBinding =>
      n.getPrefix(n.uri) == n.prefix
    }
  }

  property("getURI") = {
    Prop.forAll { (n: NamespaceBinding, s: String) =>
      n.getURI(s) != s
    }
  }

  property("getURI(prefix)") = {
    Prop.forAll { n: NamespaceBinding =>
      n.getURI(n.prefix) == n.uri
    }
  }

  property("toString") = {
    Prop.forAll { n: NamespaceBinding =>
      val str = n.toString
      Prop.atLeastOne(
        str ?= "",
        n.toString.startsWith(" xmlns:")
      )
    }
  }
}
