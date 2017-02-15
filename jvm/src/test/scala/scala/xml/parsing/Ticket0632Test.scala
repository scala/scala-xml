package scala.xml.parsing

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import scala.xml.JUnitAssertsForXML.assertEquals

class Ticket0632TestJVM {

  import scala.io.Source.fromString
  import scala.xml.parsing.ConstructingParser.fromSource
  import scala.xml.{NodeSeq, TopScope}
  private def parse(s:String) = fromSource(fromString(s), false).element(TopScope)

  @Test
  def singleAmp: Unit = {
    val expected = "<foo x=\"&amp;\"/>"
    assertEquals(expected, parse("<foo x='&amp;'/>"))
    assertEquals(expected, xml.XML.loadString("<foo x='&amp;'/>"))
  }

  @Test
  def oneAndHalfAmp: Unit = {
    val expected = "<foo x=\"&amp;amp;\"/>"
    assertEquals(expected, xml.XML.loadString("<foo x='&amp;amp;'/>"))
    assertEquals(expected, parse("<foo x='&amp;amp;'/>"))
  }

  @Test
  def doubleAmp: Unit = {
    val expected = "<foo x=\"&amp;&amp;\"/>"
    assertEquals(expected, xml.XML.loadString("<foo x='&amp;&amp;'/>"))
    assertEquals(expected, parse("<foo x='&amp;&amp;'/>"))
  }

}
