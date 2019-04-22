package scala.xml
package parsing

import scala.io.Source
import org.junit.Test
import scala.xml.JUnitAssertsForXML.{ assertEquals => assertXml }
import org.junit.Assert.assertEquals

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

  /* Example of using SYSTEM in DOCTYPE */
  @Test
  def docbookTest = {
    val xml =
      """|<!DOCTYPE docbook SYSTEM 'docbook.dtd'>
         |<book>
         |  <title>Book</title>
         |  <chapter>
         |    <title>Chapter</title>
         |    <para>Text</para>
         |  </chapter>
         |</book>""".stripMargin

    val expected = <book>
  <title>Book</title>
  <chapter>
    <title>Chapter</title>
    <para>Text</para>
  </chapter>
</book>

    val source = new Source {
      val iter = xml.iterator
      override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err) = {}
    }

    val doc = ConstructingParser.fromSource(source, true).document

    assertEquals(expected, doc.theSeq)
  }

  /* Unsupported use of lowercase DOCTYPE and SYSTEM */
  @Test(expected = classOf[scala.xml.parsing.FatalError])
  def docbookFail: Unit = {
    val xml =
      """|<!doctype docbook system 'docbook.dtd'>
         |<book>
         |<title>Book</title>
         |<chapter>
         |<title>Chapter</title>
         |<para>Text</para>
         |</chapter>
         |</book>""".stripMargin

    val source = new Source {
      val iter = xml.iterator
      override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err) = {}
    }

    ConstructingParser.fromSource(source, true).content(TopScope)
  }

  @Test
  def SI6341issue65: Unit = {
    val str = """<elem one="test" two="test2" three="test3"/>"""
    val cpa = ConstructingParser.fromSource(io.Source.fromString(str), preserveWS = true)
    val cpadoc = cpa.document()
    val ppr = new PrettyPrinter(80,5)
    val out = ppr.format(cpadoc.docElem)
    assertEquals(str, out)
  }
}
