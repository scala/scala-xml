package scala.xml.parsing

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import scala.xml.JUnitAssertsForXML.assertEquals

class PiParsingTest {


  import scala.io.Source.fromString
  import scala.xml.TopScope

  @Test
  def piNoWSLiteral: Unit = {
    val expected = "<foo>a<?pi?>b</foo>"
    assertEquals(expected, <foo>a<?pi?>b</foo>)
  }


  @Test
  def piLiteral: Unit = {
    val expected = "<foo> a <?pi?> b </foo>"
    assertEquals(expected, <foo> a <?pi?> b </foo>)
  }

}
