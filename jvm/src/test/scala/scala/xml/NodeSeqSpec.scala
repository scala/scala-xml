package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

object NodeSeqSpec extends PropertiesFor("NodeSeq")
    with NodeSeqGen {

  property("theSeq") = {
    Prop.forAll { n: NodeSeq =>
      n.theSeq ne null
    }
  }

  property("length") = {
    Prop.forAll { n: NodeSeq =>
      n.length >= 0
    }
  }

  property("\\ \"\".throws[Exception]") = {
    Prop.forAll { n: NodeSeq =>
      Prop.throws(classOf[IllegalArgumentException]) {
        (n \ "")
      }
    }
  }

  property("\\ _.throws[Exception]") = {
    Prop.forAll { n: NodeSeq =>
      Prop.iff[NodeSeq](n, {
        // FIXME: Exception thrown in NodeSeq.\.makeSeq
        case g @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            (g \ "_")
          }
        case _ => {
          (n \ "_")
          Prop.passed
        }
      })
    }
  }

  property("\\ @.throws[Exception]") = {
    Prop.forAll { n: NodeSeq =>
      Prop.iff[NodeSeq](n, {
        // FIXME: Should be IllegalArgumentException, regardless of theSeq.
        case n if n.length == 0 =>
          (n \ "@") ?= NodeSeq.Empty
        case n if n.length == 1 =>
          Prop.throws(classOf[IllegalArgumentException]) {
            (n \ "@")
          }
        case n: NodeSeq =>
          (n \ "@")
          Prop.passed
      })
    }
  }

  property("\\") = {
    Prop.forAll { (n: NodeSeq, s: String) =>
      Prop.iff[String](s, {
        // FIXME: Should be IllegalArgumentException, regardless of theSeq.
        case "" =>
          Prop.throws(classOf[IllegalArgumentException]) {
            (n \ s)
          }
        case "@" =>
          Prop.throws(classOf[IllegalArgumentException]) {
            (n \ s)
          }
        case s =>
          (n \ s)
          Prop.passed
      })
    }
  }

  property("\\\\ \"\".throws[Exception]") = {
    Prop.forAll { n: NodeSeq =>
      // FIXME: Should be IllegalArgumentException.
      Prop.throws(classOf[StringIndexOutOfBoundsException]) {
        (n \\ "")
      }
    }
  }

  property("\\\\ @.throws[Exception]") = {
    Prop.forAll { n: NodeSeq =>
      Prop.iff[NodeSeq](n, {
        // FIXME: Should be IllegalArgumentException, regardless of theSeq
        case n if n.filter(!_.isAtom).length == 0 =>
          (n \\ "@") ?= NodeSeq.Empty
        case n =>
          Prop.throws(classOf[IllegalArgumentException]) {
            (n \\ "@")
          }
      })
    }
  }

  property("\\\\") = {
    Prop.forAll { (n: NodeSeq, s: String) =>
      Prop.iff[String](s, {
        // FIXME: Should be IllegalArgumentException, regardless of theSeq.
        case "" =>
          Prop.throws(classOf[StringIndexOutOfBoundsException]) {
            (n \\ s)
          }
        case "@" =>
          Prop.throws(classOf[IllegalArgumentException]) {
            (n \\ s)
          }
        case s =>
          (n \\ s)
          Prop.passed
      })
    }
  }

  property("\\@ \"\".throws[Exception]") = {
    Prop.forAll { n: NodeSeq =>
      Prop.iff[NodeSeq](n, {
        // FIXME: Should be IllegalArgumentException, regardless of theSeq.
        case n if n.length == 0 =>
          (n \@ "") ?= ""
        case n if n.length == 1 =>
          Prop.throws(classOf[IllegalArgumentException]) {
            (n \@ "")
          }
        case s =>
          (n \@ "")
          Prop.passed
      })
    }
  }

  property("\\@ _.throws[Exception]") = {
    Prop.forAll { n: NodeSeq =>
      Prop.iff[NodeSeq](n, {
        // FIXME: Exception thrown in NodeSeq.\.makeSeq
        case g @ Group(_) =>
          Prop.throws(classOf[UnsupportedOperationException]) {
            (g \@ "_")
          }
        case _ => {
          (n \@ "_")
          Prop.passed
        }
      })
    }
  }

  property("\\@") = {
    Prop.forAll { (n: NodeSeq, s: String) =>
      // FIXME: Should be IllegalArgumentException, regardless of theSeq.
      Prop.throws(classOf[IllegalArgumentException]) {
        (n \@ s)
      } || Prop.passed // FIXME: Error conditions are too complex.
    }
  }

  property("text") = {
    Prop.forAll { n: NodeSeq =>
      n.text.length >= 0
    }
  }
}
