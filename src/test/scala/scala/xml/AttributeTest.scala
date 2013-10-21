package scala.xml

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

class AttributeTest {
  @Test
  def unprefixedAttribute: Unit = {
    val x = new UnprefixedAttribute("foo","bar", Null)
    assertEquals(Some(Text("bar")), x.get("foo"))
    assertEquals(Text("bar"), x("foo"))
    assertEquals(None, x.get("no_foo"))
    assertEquals(null, x("no_foo"))

    val y = x.remove("foo")
    assertEquals(Null, y)

    val z = new UnprefixedAttribute("foo", null:NodeSeq, x)
    assertEquals(None, z.get("foo"))

    var appended = x append x append x append x
    var len = 0; while (appended ne Null) {
      appended = appended.next
      len = len + 1
    }
    assertEquals("removal of duplicates for unprefixed attributes in append", 1, len)
  }

  @Test
  def attributeWithOption: Unit = {
    val x = new UnprefixedAttribute("foo", Some(Text("bar")), Null)

    assertEquals(Some(Text("bar")), x.get("foo"))
    assertEquals(Text("bar"), x("foo"))
    assertEquals(None, x.get("no_foo"))
    assertEquals(null, x("no_foo"))

    val attr1 = Some(Text("foo value"))
    val attr2 = None
    val y = <b foo={attr1} bar={attr2} />
    assertEquals(Some(Text("foo value")), y.attributes.get("foo"))
    assertEquals(Text("foo value"), y.attributes("foo"))
    assertEquals(None, y.attributes.get("bar"))
    assertEquals(null, y.attributes("bar"))

    val z = new UnprefixedAttribute("foo", None, x)
    assertEquals(None, z.get("foo"))
  }

  @Test
  def attributeToString: Unit = {
    val expected: String = """<b x="&amp;"/>"""
    assertEquals(expected, (<b x="&amp;"/>).toString)
    assertEquals(expected, (<b x={"&"}/>).toString)
  }

  @Test
  def attributeOperator: Unit = {
    val xml = <foo bar="apple" />
    assertEquals("apple", xml \@ "bar")
  }

}