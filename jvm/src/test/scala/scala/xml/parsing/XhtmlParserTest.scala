package scala.xml
package parsing

import scala.io.Source

import org.junit.Test
import org.junit.Assert.assertEquals

class XhtmlParserTest {

  @Test
  def issue259(): Unit = {
    val xml: String =
      """|<!DOCTYPE html>
         |<html xmlns="http://www.w3.org/1999/xhtml">
         |  <head>
         |    <meta charset="utf-8"/>
         |  </head>
         |  <body>
         |    <p>Text</p>
         |  </body>
         |</html>""".stripMargin

    val expected: Elem = <html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta charset="utf-8"/>
  </head>
  <body>
    <p>Text</p>
  </body>
</html>

    assertEquals(expected, XhtmlParser(Source.fromString(xml)).theSeq)
  }

  @Test
  def html4Strict(): Unit = {
    val xml: String =
      """|<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
         |    "http://www.w3.org/TR/html4/strict.dtd">
         |<html>
         |  <head>
         |    <title>Title</title>
         |  </head>
         |  <body>
         |    <p>Text</p>
         |  </body>
         |</html>""".stripMargin

    val expected: Elem = <html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>Title</title>
  </head>
  <body>
    <p>Text</p>
  </body>
</html>

    assertEquals(expected, XhtmlParser(Source.fromString(xml)).theSeq)
  }
}
