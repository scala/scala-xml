package scala.xml

import scala.collection.Seq
import org.junit.Test
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

  @Test
  def attributePathRootNoAttribute: Unit = {
    val xml = <foo />
    assertEquals(NodeSeq.Empty, xml \ "@bar")
  }

  @Test(expected=classOf[IllegalArgumentException])
  def attributePathIllegalEmptyAttribute: Unit = {
    val xml = <foo />
    xml \ "@"
  }

  @Test
  def attributePathRootWithOneAttribute: Unit = {
    val xml = <foo bar="apple" />
    assertEquals(Group(Text("apple")), xml \ "@bar")
    // assertEquals(NodeSeq.fromSeq(Seq(Text("apple"))), xml \ "@bar")
  }

  @Test
  def attributePathRootWithMissingAttributes: Unit = {
    val xml = <foo bar="apple" />
    assertEquals(NodeSeq.Empty, xml \ "@oops")
  }

  @Test
  def attributePathDuplicateAttribute: Unit = {
    val xml = Elem(null, "foo",
      Attribute("bar", Text("apple"),
        Attribute("bar", Text("orange"), Null)), TopScope)
    assertEquals(Group(Text("apple")), xml \ "@bar")
  }

  @Test
  def attributePathDescendantAttributes: Unit = {
    val xml = <a><b bar="1" /><b bar="2" /></a>
    assertEquals(NodeSeq.fromSeq(Seq(Text("1"), Text("2"))), (xml \\ "@bar"))
  }

  @Test
  def attributeDescendantPathChildAttributes: Unit = {
    val xml = <a><b bar="1" /><b bar="2" /></a>
    assertEquals(NodeSeq.fromSeq(Seq(Text("1"), Text("2"))), (xml \ "b" \\ "@bar"))
  }

  @Test
  def attributeDescendantPathDescendantAttributes: Unit = {
    val xml = <a><b bar="1" /><b bar="2" /></a>
    assertEquals(NodeSeq.fromSeq(Seq(Text("1"), Text("2"))), (xml \\ "b" \\ "@bar"))
  }

  @Test
  def attributeChildDescendantPathDescendantAttributes: Unit = {
    val xml = <x><a><b bar="1" /><b bar="2" /></a></x>
    assertEquals(NodeSeq.fromSeq(Seq(Text("1"), Text("2"))), (xml \ "a" \\ "@bar"))
  }

  @Test
  def attributeDescendantDescendantPathDescendantAttributes: Unit = {
    val xml = <x><a><b bar="1" /><b bar="2" /></a></x>
    assertEquals(NodeSeq.fromSeq(Seq(Text("1"), Text("2"))), (xml \\ "b" \\ "@bar"))
  }

  @Test(expected=classOf[IllegalArgumentException])
  def attributePathDescendantIllegalEmptyAttribute: Unit = {
    val xml = <foo />
    xml \\ "@"
  }

  @Test
  def attributePathNoDescendantAttributes: Unit = {
    val xml = <a><b bar="1" /><b bar="2" /></a>
    assertEquals(NodeSeq.Empty, (xml \\ "@oops"))
  }

  @Test
  def attributePathOneChildWithAttributes: Unit = {
    val xml = <a><b bar="1" />></a>
    assertEquals(Group(Seq(Text("1"))), (xml \ "b" \ "@bar"))
  }

  @Test
  def attributePathTwoChildrenWithAttributes: Unit = {
    val xml = <a><b bar="1" /><b bar="2" /></a>
    val b = xml \ "b"
    assertEquals(2, b.length)
    assertEquals(NodeSeq.fromSeq(Seq(<b bar="1"/>, <b bar="2"/>)), b)
    val barFail = b \ "@bar"
    val barList =  b.map(_ \ "@bar")
    assertEquals(NodeSeq.Empty, barFail)
    assertEquals(List(Group(Seq(Text("1"))), Group(Seq(Text("2")))), barList)
  }

  @Test(expected=classOf[IllegalArgumentException])
  def invalidAttributeFailForOne: Unit = {
    <x/> \ "@"
  }

  @Test(expected=classOf[IllegalArgumentException])
  def invalidAttributeFailForMany: Unit = {
    <x><y/><z/></x>.child \ "@"
  }

  @Test(expected=classOf[IllegalArgumentException])
  def invalidEmptyAttributeFailForOne: Unit = {
    <x/> \@ ""
  }

  @Test(expected=classOf[IllegalArgumentException])
  def invalidEmptyAttributeFailForMany: Unit = {
    <x><y/><z/></x>.child \@ ""
  }
}
