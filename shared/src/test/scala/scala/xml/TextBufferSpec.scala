package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }

object TextBufferSpec extends CheckProperties("TextBuffer")
    with TextBufferGen {

  // FIXME: Verify result value.
  property("fromString") = {
    Prop.forAll { str: String =>
      TextBuffer.fromString(str)
      Prop.passed
    }
  }

  // FIXME: Verify result value.
  property("append") = {
    Prop.forAll { (tb: TextBuffer, cs: Seq[Char]) =>
      tb.append(cs)
      Prop.passed
    }
  }

  // FIXME: Verify result value.
  property("toText") = {
    Prop.forAll { tb: TextBuffer =>
      tb.toText
      Prop.passed
    }
  }
}
