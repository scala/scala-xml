package scala.xml
package dtd

import org.junit.Test
import org.junit.Assert.assertEquals

class DeclTest {

  @Test
  def elemDeclToString: Unit = {
    assertEquals(
      "<!ELEMENT x (#PCDATA)>",
      ElemDecl("x", PCDATA).toString
    )
  }

  @Test
  def attListDeclToString: Unit = {

    val expected =
      """|<!ATTLIST x
         |  y CDATA #REQUIRED
         |  z CDATA #REQUIRED>""".stripMargin

    val actual = AttListDecl("x",
      List(
        AttrDecl("y", "CDATA", REQUIRED),
        AttrDecl("z", "CDATA", REQUIRED)
      )
    ).toString

    assertEquals(expected, actual)
  }

  @Test
  def parsedEntityDeclToString: Unit = {
    assertEquals(
      """<!ENTITY foo SYSTEM "bar">""",
      ParsedEntityDecl("foo", ExtDef(SystemID("bar"))).toString
    )
  }
}
