package scala.xml

import org.junit.Test

class BillionLaughsAttackTest {

  /**
   * org.xml.sax.SAXParseException: JAXP00010001: The parser has
   * encountered more than "64000" entity expansions in this document;
   * this is the limit imposed by the JDK.
   */
  @Test(expected=classOf[org.xml.sax.SAXParseException])
  def lolzTest: Unit = {
    XML.loadString(lolz)
  }

  // Copied from https://msdn.microsoft.com/en-us/magazine/ee335713.aspx
  val lolz =
    """<?xml version="1.0"?>
      |<!DOCTYPE lolz [
      | <!ENTITY lol "lol">
      | <!ELEMENT lolz (#PCDATA)>
      | <!ENTITY lol1 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
      | <!ENTITY lol2 "&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;">
      | <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
      | <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
      | <!ENTITY lol5 "&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;">
      | <!ENTITY lol6 "&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;">
      | <!ENTITY lol7 "&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;">
      | <!ENTITY lol8 "&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;">
      | <!ENTITY lol9 "&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;">
      |]>
      |<lolz>&lol9;</lolz>
      |""".stripMargin

}
