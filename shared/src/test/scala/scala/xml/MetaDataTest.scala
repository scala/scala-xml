package scala.xml

import org.junit.Test
import org.junit.Assert.assertEquals

class MetaDataTest {

  @Test
  def absentElementPrefixed1: Unit = {
    // type ascription to help overload resolution pick the right variant
    assertEquals(null: Object, Null("za://foo.com", TopScope, "bar"))
    assertEquals(null, Null("bar"))
  }

  @Test
  def absentElementPrefixed2: Unit = {
    assertEquals(Option.empty, Null.get("za://foo.com", TopScope, "bar" ))
    assertEquals(Option.empty, Null.get("bar"))
  }

  @Test
  def presentElement1: Unit = {
    val x = new PrefixedAttribute("zo","bar", new Atom(42), Null)
    val s = NamespaceBinding("zo","za://foo.com", TopScope)
    assertEquals(new Atom(42), x("za://foo.com", s, "bar" ))
    assertEquals(null, x("bar"))
    assertEquals(Some(new Atom(42)), x.get("za://foo.com", s, "bar"))
    assertEquals(Option.empty, x.get("bar"))
  }

  @Test
  def presentElement2: Unit = {
    val s = NamespaceBinding("zo","za://foo.com", TopScope)
    val x1 = new PrefixedAttribute("zo","bar", new Atom(42), Null)
    val x = new UnprefixedAttribute("bar","meaning", x1)
    assertEquals(null, x(null, s, "bar"))
    assertEquals(Text("meaning"), x("bar"))
    assertEquals(None, x.get(null, s, "bar" ))
    assertEquals(Some(Text("meaning")), x.get("bar"))
  }

  @Test
  def attributeExtractor: Unit = {
    def domatch(x:Node): Node = {
      x match {
            case Node("foo", md @ UnprefixedAttribute(_, value, _), _*) if value.nonEmpty =>
                 md("bar")(0)
            case _ => new Atom(3)
      }
    }
    val z =  <foo bar="gar"/>
    val z2 = <foo/>
    assertEquals(Text("gar"), domatch(z))
    assertEquals(new Atom(3), domatch(z2))
  }

  @Test
  def reverseTest: Unit = {
    assertEquals("", Null.reverse.toString)
    assertEquals(""" b="c"""", <a b="c"/>.attributes.reverse.toString)
    assertEquals(""" d="e" b="c"""", <a b="c" d="e"/>.attributes.reverse.toString)
  }

}
