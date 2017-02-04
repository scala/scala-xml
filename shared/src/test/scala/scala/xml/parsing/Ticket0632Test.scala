package scala.xml.parsing

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import scala.xml.JUnitAssertsForXML.assertEquals

class Ticket0632Test {

  import scala.io.Source.fromString
  import scala.xml.{NodeSeq, TopScope}

  @Test
  def singleAmp: Unit = {
    val expected = "<foo x=\"&amp;\"/>"
    assertEquals(expected, <foo x="&amp;"/>)
    assertEquals(expected, <foo x={ "&" }/>)
  }

  @Test
  def oneAndHalfAmp: Unit = {
    val expected = "<foo x=\"&amp;amp;\"/>"
    assertEquals(expected, <foo x="&amp;amp;"/>)
    assertEquals(expected, <foo x={ "&amp;" }/>)
  }

  @Test
  def doubleAmp: Unit = {
    val expected = "<foo x=\"&amp;&amp;\"/>"
    assertEquals(expected, <foo x="&amp;&amp;"/>)
    assertEquals(expected, <foo x={ "&&" }/>)
  }

}
