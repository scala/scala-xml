package scala.xml

import org.junit.Test
import JUnitAssertsForXML.assertEquals

class PrintEmptyElementsTest {

  @Test
  def representEmptyXMLElementsInShortForm: Unit = {
    val expected: String =
      """|
         |<hi/> <!-- literal short -->
         |<there></there> <!-- literal long -->
         |<guys who="you all"></guys> <!-- literal long with attribute-->
         |<hows it="going"/> <!-- literal short with attribute -->
         |<this>is pretty cool</this> <!-- literal not empty -->
         |""".stripMargin
    // the xml snippet is not indented because indentation affects pretty printing
    // results
    val actual: NodeSeq =
      <xml:group>
<hi/> <!-- literal short -->
<there></there> <!-- literal long -->
<guys who="you all"></guys> <!-- literal long with attribute-->
<hows it="going"/> <!-- literal short with attribute -->
<this>is pretty cool</this> <!-- literal not empty -->
</xml:group>
    assertEquals(expected, actual)
  }

  @Test
  def programmaticLong: Unit = {
    assertEquals(
      "<emptiness></emptiness> <!--programmatic long-->",
      Elem(null, "emptiness", Null, TopScope, false) ++ Text(" ") ++ Comment(
        "programmatic long"
      )
    )
  }

  @Test
  def programmaticShort: Unit = {
    assertEquals(
      "<vide/> <!--programmatic short-->",
      Elem(null, "vide", Null, TopScope, true) ++ Text(" ") ++ Comment(
        "programmatic short"
      )
    )
  }

  @Test
  def programmaticShortWithAttribute: Unit = {
    assertEquals(
      """<elem attr="value"/> <!--programmatic short with attribute-->""",
      Elem(
        null,
        "elem",
        Attribute("attr", Text("value"), Null),
        TopScope,
        true
      ) ++ Text(" ") ++ Comment("programmatic short with attribute")
    )
  }

  @Test
  def programmaticLongWithAttribute: Unit = {
    assertEquals(
      """<elem2 attr2="value2"></elem2> <!--programmatic long with attribute-->""",
      Elem(
        null,
        "elem2",
        Attribute("attr2", Text("value2"), Null),
        TopScope,
        false
      ) ++ Text(" ") ++ Comment("programmatic long with attribute")
    )
  }

}
