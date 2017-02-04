package scala.xml
package pull

import org.junit.Test
import org.junit.Assert.{assertFalse, assertTrue}

import scala.io.Source
import scala.xml.parsing.FatalError

class XMLEventReaderTest {

  val src = Source.fromString("<hello><world/>!</hello>")

  private def toSource(s: String) = new Source {
    val iter = s.iterator
    override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err) {}
  }

}
