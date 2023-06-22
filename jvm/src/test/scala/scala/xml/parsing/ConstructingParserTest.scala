package scala.xml
package parsing

import scala.io.Source
import org.junit.Test
import scala.xml.JUnitAssertsForXML.{ assertEquals => assertXml }
import org.junit.Assert.assertEquals

class ConstructingParserTest {

  @Test
  def t9060(): Unit = {
    val a: String = """<a xmlns:b·="http://example.com"/>"""
    val source: Source = new Source {
      override val iter: Iterator[Char] = a.iterator
      override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err): Unit = ()
    }
    val doc: NodeSeq = ConstructingParser.fromSource(source, preserveWS = false).content(TopScope)
    assertXml(a, doc)
  }

  /* Example of using SYSTEM in DOCTYPE */
  @Test
  def docbookTest(): Unit = {
    val xml: String =
      """|<!DOCTYPE docbook SYSTEM 'docbook.dtd'>
         |<book>
         |  <title>Book</title>
         |  <chapter>
         |    <title>Chapter</title>
         |    <para>Text</para>
         |  </chapter>
         |</book>""".stripMargin

    val expected: Elem = <book>
  <title>Book</title>
  <chapter>
    <title>Chapter</title>
    <para>Text</para>
  </chapter>
</book>

    val source: Source = new Source {
      override val iter: Iterator[Char] = xml.iterator
      override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err): Unit = ()
    }

    val doc: Document = ConstructingParser.fromSource(source, preserveWS = true).document()

    assertEquals(expected, doc.theSeq)
  }

  /* Unsupported use of lowercase DOCTYPE and SYSTEM */
  @Test(expected = classOf[scala.xml.parsing.FatalError])
  def docbookFail(): Unit = {
    val xml: String =
      """|<!doctype docbook system 'docbook.dtd'>
         |<book>
         |<title>Book</title>
         |<chapter>
         |<title>Chapter</title>
         |<para>Text</para>
         |</chapter>
         |</book>""".stripMargin

    val source: Source = new Source {
      override val iter: Iterator[Char] = xml.iterator
      override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err): Unit = ()
    }

    ConstructingParser.fromSource(source, preserveWS = true).content(TopScope)
  }

  @Test
  def SI6341issue65(): Unit = {
    val str: String = """<elem one="test" two="test2" three="test3"/>"""
    val cpa: ConstructingParser = ConstructingParser.fromSource(Source.fromString(str), preserveWS = true)
    val cpadoc: Document = cpa.document()
    val ppr: PrettyPrinter = new PrettyPrinter(80,5)
    val out: String = ppr.format(cpadoc.docElem)
    assertEquals(str, out)
  }

  // https://github.com/scala/scala-xml/issues/541
  @Test
  def issue541(): Unit = {
    val xml: String =
      """|<script>// <![CDATA[
         |[]; // ]]>
         |</script>""".stripMargin
    val parser: ConstructingParser = ConstructingParser.fromSource(Source.fromString(xml), preserveWS = true)
    parser.document().docElem  // shouldn't crash
  }

  @Test(expected = classOf[scala.xml.parsing.FatalError])
  def issue656(): Unit = {
    // mismatched quotes should not cause an infinite loop
    XhtmlParser(Source.fromString("""<html><body myAttribute='value"/></html>"""))
  }
}
