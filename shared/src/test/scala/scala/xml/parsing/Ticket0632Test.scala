package scala.xml.parsing

import org.junit.Test
import scala.xml.JUnitAssertsForXML.assertEquals

class Ticket0632Test {

  @Test
  def singleAmp: Unit = {
    val expected = "<foo x=\"&amp;\"/>"
    assertEquals(expected, <foo x="&amp;"/>)
    assertEquals(expected, <foo x={"&"}/>)
  }

  @Test
  def oneAndHalfAmp: Unit = {
    val expected = "<foo x=\"&amp;amp;\"/>"
    assertEquals(expected, <foo x="&amp;amp;"/>)
    assertEquals(expected, <foo x={"&amp;"}/>)
  }

  @Test
  def doubleAmp: Unit = {
    val expected = "<foo x=\"&amp;&amp;\"/>"
    assertEquals(expected, <foo x="&amp;&amp;"/>)
    assertEquals(expected, <foo x={"&&"}/>)
  }

}
