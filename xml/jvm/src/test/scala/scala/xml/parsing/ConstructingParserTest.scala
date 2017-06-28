package scala.xml
package parsing

import scala.io.Source
import org.junit.Test
import scala.xml.JUnitAssertsForXML.{ assertEquals => assertXml }

class ConstructingParserTest {

  @Test
  def t9060 = {
    val a = """<a xmlns:b·="http://example.com"/>"""
    val source = new Source {
      val iter = a.iterator
      override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err) = {}
    }
    val doc = ConstructingParser.fromSource(source, false).content(TopScope)
    assertXml(a, doc)

  }

}
