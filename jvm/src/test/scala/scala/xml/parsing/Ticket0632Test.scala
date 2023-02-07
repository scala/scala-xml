package scala.xml.parsing

import org.junit.Test
import scala.xml.JUnitAssertsForXML.assertEquals
import scala.xml.NodeSeq

class Ticket0632TestJVM {

  import scala.io.Source.fromString
  import scala.xml.parsing.ConstructingParser.fromSource
  import scala.xml.TopScope
  private def parse(s:String): NodeSeq = fromSource(fromString(s), preserveWS = false).element(TopScope)

  @Test
  def singleAmp(): Unit = {
    val expected: String = "<foo x=\"&amp;\"/>"
    assertEquals(expected, parse("<foo x='&amp;'/>"))
    assertEquals(expected, xml.XML.loadString("<foo x='&amp;'/>"))
  }

  @Test
  def oneAndHalfAmp(): Unit = {
    val expected: String = "<foo x=\"&amp;amp;\"/>"
    assertEquals(expected, xml.XML.loadString("<foo x='&amp;amp;'/>"))
    assertEquals(expected, parse("<foo x='&amp;amp;'/>"))
  }

  @Test
  def doubleAmp(): Unit = {
    val expected: String = "<foo x=\"&amp;&amp;\"/>"
    assertEquals(expected, xml.XML.loadString("<foo x='&amp;&amp;'/>"))
    assertEquals(expected, parse("<foo x='&amp;&amp;'/>"))
  }
}
