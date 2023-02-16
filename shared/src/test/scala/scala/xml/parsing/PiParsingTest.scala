package scala.xml.parsing

import org.junit.Test
import scala.xml.JUnitAssertsForXML.assertEquals

class PiParsingTest {

  @Test
  def piNoWSLiteral(): Unit = {
    val expected: String = "<foo>a<?pi?>b</foo>"
    assertEquals(expected, <foo>a<?pi?>b</foo>)
  }

  @Test
  def piLiteral(): Unit = {
    val expected: String = "<foo> a <?pi?> b </foo>"
    assertEquals(expected, <foo> a <?pi?> b </foo>)
  }
}
