package scala.xml

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

object XMLTest {
  val e: scala.xml.MetaData = Null //Node.NoAttributes
  val sc: scala.xml.NamespaceBinding = TopScope
}

class XMLTest {
  import XMLTest.{ e, sc }

  @Test
  def nodeSeq: Unit = {
    val p = <foo>
              <bar gt='ga' value="3"/>
              <baz bazValue="8"/>
              <bar value="5" gi='go'/>
            </foo>

    val pelems_1 = for (x <- p \ "bar"; y <- p \ "baz") yield {
      Text(x.attributes("value").toString + y.attributes("bazValue").toString + "!")
    };

    val pelems_2 = new NodeSeq { val theSeq = List(Text("38!"), Text("58!")) };
    assertTrue(pelems_1 sameElements pelems_2)
    assertTrue(Text("8") sameElements (p \\ "@bazValue"))
  }

  @Test
  def queryBooks: Unit = {
    val books =
      <bks>
        <book><title>Blabla</title></book>
        <book><title>Blubabla</title></book>
        <book><title>Baaaaaaalabla</title></book>
      </bks>;

    val reviews =
      <reviews>
        <entry>
          <title>Blabla</title>
          <remarks>
            Hallo Welt.
          </remarks>
        </entry>
        <entry>
          <title>Blubabla</title>
          <remarks>
            Hello Blu
          </remarks>
        </entry>
        <entry>
          <title>Blubabla</title>
          <remarks>
            rem 2
          </remarks>
        </entry>
      </reviews>;

    val results1 = new scala.xml.PrettyPrinter(80, 5).formatNodes(
      for (
        t <- books \\ "title";
        r <- reviews \\ "entry" if (r \ "title") xml_== t
      ) yield <result>
                { t }
                { r \ "remarks" }
              </result>);
    val results1Expected = """<result>
    |     <title>Blabla</title>
    |     <remarks> Hallo Welt. </remarks>
    |</result><result>
    |     <title>Blubabla</title>
    |     <remarks> Hello Blu </remarks>
    |</result><result>
    |     <title>Blubabla</title>
    |     <remarks> rem 2 </remarks>
    |</result>""".stripMargin
    assertEquals(results1Expected, results1)

    {
      val actual = for (t @ <book><title>Blabla</title></book> <- new NodeSeq { val theSeq = books.child }.toList)
        yield t
      val expected = List(<book><title>Blabla</title></book>)
      assertEquals(expected, actual)
    }

  }

  @Test
  def queryPhoneBook: Unit = {
    val phoneBook =
      <phonebook>
        <descr>
          This is the<b>phonebook</b>
          of the
          <a href="http://acme.org">ACME</a>
          corporation.
        </descr>
        <entry>
          <name>John</name>
          <phone where="work">  +41 21 693 68 67</phone>
          <phone where="mobile">+41 79 602 23 23</phone>
        </entry>
      </phonebook>;

    val addrBook =
      <addrbook>
        <descr>
          This is the<b>addressbook</b>
          of the
          <a href="http://acme.org">ACME</a>
          corporation.
        </descr>
        <entry>
          <name>John</name>
          <street> Elm Street</street>
          <city>Dolphin City</city>
        </entry>
      </addrbook>;

    val actual: String = new scala.xml.PrettyPrinter(80, 5).formatNodes(
      for (
        t <- addrBook \\ "entry";
        r <- phoneBook \\ "entry" if (t \ "name") xml_== (r \ "name")
      ) yield <result>
                { t.child }
                { r \ "phone" }
              </result>)
    val expected = """|<result>
      |     <name>John</name>
      |     <street> Elm Street</street>
      |     <city>Dolphin City</city>
      |     <phone where="work"> +41 21 693 68 67</phone>
      |     <phone where="mobile">+41 79 602 23 23</phone>
      |</result>""".stripMargin
    assertEquals(expected, actual)
  }

  @Test
  def namespaces: Unit = {
    val cuckoo = <cuckoo xmlns="http://cuckoo.com">
                   <foo/>
                   <bar/>
                 </cuckoo>;
    assertEquals("http://cuckoo.com", cuckoo.namespace)
    for (n <- cuckoo \ "_") {
      assertEquals("http://cuckoo.com", n.namespace)
    }
  }

  @Test
  def validationOfElements: Unit = {
    val vtor = new scala.xml.dtd.ElementValidator();
    {
      import scala.xml.dtd.ELEMENTS
      import scala.xml.dtd.ContentModel._
      vtor.setContentModel(
        ELEMENTS(
          Sequ(
            Letter(ElemName("bar")),
            Star(Letter(ElemName("baz"))))));
    }
    assertTrue(vtor(<foo><bar/><baz/><baz/></foo>))

    {
      import scala.xml.dtd.MIXED
      import scala.xml.dtd.ContentModel._

      vtor.setContentModel(
        MIXED(
          Alt(Letter(ElemName("bar")),
            Letter(ElemName("baz")),
            Letter(ElemName("bal")))));
    }

    assertTrue(vtor(<foo><bar/><baz/><baz/></foo>))
    assertTrue(vtor(<foo>ab<bar/>cd<baz/>ed<baz/>gh</foo>))
    assertFalse(vtor(<foo> <ugha/> <bugha/> </foo>))
  }

  def validationfOfAttributes: Unit = {
    val vtor = new scala.xml.dtd.ElementValidator();
    vtor.setContentModel(null)
    vtor.setMetaData(List())
    assertFalse(vtor(<foo bar="hello"/>))

    {
      import scala.xml.dtd._
      vtor setMetaData List(AttrDecl("bar", "CDATA", IMPLIED))
    }
    assertFalse(vtor(<foo href="http://foo.com" bar="hello"/>))
    assertTrue(vtor(<foo bar="hello"/>))

    {
      import scala.xml.dtd._
      vtor.setMetaData(List(AttrDecl("bar", "CDATA", REQUIRED)))
    }
    assertFalse(vtor(<foo href="http://foo.com"/>))
    assertTrue(vtor(<foo bar="http://foo.com"/>))
  }

  import java.io.StringReader
  import org.xml.sax.InputSource

  def Elem(prefix: String, label: String, attributes: MetaData, scope: NamespaceBinding, child: Node*): Elem =
    scala.xml.Elem.apply(prefix, label, attributes, scope, minimizeEmpty = true, child: _*)

  lazy val parsedxml1 = XML.load(new InputSource(new StringReader("<hello><world/></hello>")))
  lazy val parsedxml11 = XML.load(new InputSource(new StringReader("<hello><world/></hello>")))
  val xmlFile2 = "<bib><book><author>Peter Buneman</author><author>Dan Suciu</author><title>Data on ze web</title></book><book><author>John Mitchell</author><title>Foundations of Programming Languages</title></book></bib>";
  lazy val parsedxml2 = XML.load(new InputSource(new StringReader(xmlFile2)))

  @Test
  def equality = {
    val c = new Node {
      def label = "hello"
      override def hashCode() =
        Utility.hashCode(prefix, label, attributes.hashCode(), scope.hashCode(), child);
      def child = Elem(null, "world", e, sc);
      //def attributes = e;
      override def text = ""
    }

    assertTrue(c == parsedxml11)
    assertTrue(parsedxml1 == parsedxml11)
    assertTrue(List(parsedxml1) sameElements List(parsedxml11))
    assertTrue(Array(parsedxml1).toList sameElements List(parsedxml11))

    val x2 = "<book><author>Peter Buneman</author><author>Dan Suciu</author><title>Data on ze web</title></book>";

    val i = new InputSource(new StringReader(x2))
    val x2p = scala.xml.XML.load(i)

    assertTrue(x2p == Elem(null, "book", e, sc,
      Elem(null, "author", e, sc, Text("Peter Buneman")),
      Elem(null, "author", e, sc, Text("Dan Suciu")),
      Elem(null, "title", e, sc, Text("Data on ze web"))))

  }

  @Test
  def xpath = {
    assertTrue(parsedxml1 \ "_" sameElements List(Elem(null, "world", e, sc)))

    assertTrue(parsedxml1 \ "world" sameElements List(Elem(null, "world", e, sc)))

    assertTrue(
      (parsedxml2 \ "_") sameElements List(
        Elem(null, "book", e, sc,
          Elem(null, "author", e, sc, Text("Peter Buneman")),
          Elem(null, "author", e, sc, Text("Dan Suciu")),
          Elem(null, "title", e, sc, Text("Data on ze web"))),
        Elem(null, "book", e, sc,
          Elem(null, "author", e, sc, Text("John Mitchell")),
          Elem(null, "title", e, sc, Text("Foundations of Programming Languages")))))
    assertTrue((parsedxml2 \ "author").isEmpty)

    assertTrue(
      (parsedxml2 \ "book") sameElements List(
        Elem(null, "book", e, sc,
          Elem(null, "author", e, sc, Text("Peter Buneman")),
          Elem(null, "author", e, sc, Text("Dan Suciu")),
          Elem(null, "title", e, sc, Text("Data on ze web"))),
        Elem(null, "book", e, sc,
          Elem(null, "author", e, sc, Text("John Mitchell")),
          Elem(null, "title", e, sc, Text("Foundations of Programming Languages")))))

    assertTrue(
      (parsedxml2 \ "_" \ "_") sameElements List(
        Elem(null, "author", e, sc, Text("Peter Buneman")),
        Elem(null, "author", e, sc, Text("Dan Suciu")),
        Elem(null, "title", e, sc, Text("Data on ze web")),
        Elem(null, "author", e, sc, Text("John Mitchell")),
        Elem(null, "title", e, sc, Text("Foundations of Programming Languages"))))

    assertTrue(
      (parsedxml2 \ "_" \ "author") sameElements List(
        Elem(null, "author", e, sc, Text("Peter Buneman")),
        Elem(null, "author", e, sc, Text("Dan Suciu")),
        Elem(null, "author", e, sc, Text("John Mitchell"))))

    assertTrue((parsedxml2 \ "_" \ "_" \ "author").isEmpty)
  }

  @Test
  def xpathDESCENDANTS = {
    assertTrue(
      (parsedxml2 \\ "author") sameElements List(
        Elem(null, "author", e, sc, Text("Peter Buneman")),
        Elem(null, "author", e, sc, Text("Dan Suciu")),
        Elem(null, "author", e, sc, Text("John Mitchell"))))

    assertTrue(
      (parsedxml2 \\ "title") sameElements List(
        Elem(null, "title", e, sc, Text("Data on ze web")),
        Elem(null, "title", e, sc, Text("Foundations of Programming Languages"))))

    assertEquals("<book><author>Peter Buneman</author><author>Dan Suciu</author><title>Data on ze web</title></book>",
      (parsedxml2 \\ "book") { n: Node => (n \ "title") xml_== "Data on ze web" } toString)

    assertTrue(
      ((new NodeSeq { val theSeq = List(parsedxml2) }) \\ "_") sameElements List(
        Elem(null, "bib", e, sc,
          Elem(null, "book", e, sc,
            Elem(null, "author", e, sc, Text("Peter Buneman")),
            Elem(null, "author", e, sc, Text("Dan Suciu")),
            Elem(null, "title", e, sc, Text("Data on ze web"))),
          Elem(null, "book", e, sc,
            Elem(null, "author", e, sc, Text("John Mitchell")),
            Elem(null, "title", e, sc, Text("Foundations of Programming Languages")))),
        Elem(null, "book", e, sc,
          Elem(null, "author", e, sc, Text("Peter Buneman")),
          Elem(null, "author", e, sc, Text("Dan Suciu")),
          Elem(null, "title", e, sc, Text("Data on ze web"))),
        Elem(null, "author", e, sc, Text("Peter Buneman")),
        Elem(null, "author", e, sc, Text("Dan Suciu")),
        Elem(null, "title", e, sc, Text("Data on ze web")),
        Elem(null, "book", e, sc,
          Elem(null, "author", e, sc, Text("John Mitchell")),
          Elem(null, "title", e, sc, Text("Foundations of Programming Languages"))),
        Elem(null, "author", e, sc, Text("John Mitchell")),
        Elem(null, "title", e, sc, Text("Foundations of Programming Languages"))))
  }

  @Test
  def groupNode = {
    val zx1: Node = Group { <a/><b/><c/> }
    val zy1: Node = <f>{ zx1 }</f>
    assertEquals("<f><a/><b/><c/></f>", zy1.toString)

    assertEquals("<a/><f><a/><b/><c/></f><a/><b/><c/>",
      Group { List(<a/>, zy1, zx1) }.toString)

    val zz1 = <xml:group><a/><b/><c/></xml:group>

    assertTrue(zx1 xml_== zz1)
    assertTrue(zz1.length == 3)
  }

  @Test
  def unparsed = {
    //    println("attribute value normalization")
    val xmlAttrValueNorm = "<personne id='p0003' nom='&#x015e;ahingz' />";
    {
      val isrcA = new InputSource(new StringReader(xmlAttrValueNorm));
      val parsedxmlA = XML.load(isrcA);
      val c = (parsedxmlA \ "@nom").text.charAt(0);
      assertTrue(c == '\u015e');
    }
    // buraq: if the following test fails with 'character x not allowed', it is
    //        related to the mutable variable in a closures in MarkupParser.parsecharref
    {
      val isr = scala.io.Source.fromString(xmlAttrValueNorm);
      val pxmlB = scala.xml.parsing.ConstructingParser.fromSource(isr, false);
      val parsedxmlB = pxmlB.element(TopScope);
      val c = (parsedxmlB \ "@nom").text.charAt(0);
      assertTrue(c == '\u015e');
    }

    // #60 test by round trip

    val p = scala.xml.parsing.ConstructingParser.fromSource(scala.io.Source.fromString("<foo bar:attr='&amp;'/>"), true)
    val n = p.element(new scala.xml.NamespaceBinding("bar", "BAR", scala.xml.TopScope))(0)
    assertTrue(n.attributes.get("BAR", n, "attr").nonEmpty)
  }

  @Test
  def dodgyNamespace = {
    val x = <flog xmlns:ee="http://ee.com"><foo xmlns:dog="http://dog.com"><dog:cat/></foo></flog>
    assertTrue(x.toString.matches(".*xmlns:dog=\"http://dog.com\".*"));
  }

  import NodeSeq.seqToNodeSeq

  val ax = <hello foo="bar" x:foo="baz" xmlns:x="the namespace from outer space">
             <world/>
           </hello>

  val cx = <z:hello foo="bar" xmlns:z="z" x:foo="baz" xmlns:x="the namespace from outer space">
             crazy text world
           </z:hello>

  val bx = <hello foo="bar&amp;x"></hello>

  @Test
  def XmlEx = {
    assertTrue((ax \ "@foo") xml_== "bar") // uses NodeSeq.view!
    assertTrue((ax \ "@foo") xml_== xml.Text("bar")) // dto.
    assertTrue((bx \ "@foo") xml_== "bar&x") // dto.
    assertTrue((bx \ "@foo") xml_sameElements List(xml.Text("bar&x")))
    assertTrue("<hello foo=\"bar&amp;x\"></hello>" == bx.toString)
  }

  @Test
  def XmlEy {
    val z = ax \ "@{the namespace from outer space}foo"
    assertTrue((ax \ "@{the namespace from outer space}foo") xml_== "baz")
    assertTrue((cx \ "@{the namespace from outer space}foo") xml_== "baz")

    try {
      ax \ "@"
      assertTrue(false)
    } catch {
      case _: IllegalArgumentException =>
    }
    try {
      ax \ "@{"
      assertTrue(false)
    } catch {
      case _: IllegalArgumentException =>
    }
    try {
      ax \ "@{}"
      assertTrue(false)
    } catch {
      case _: IllegalArgumentException =>
    }

  }

  @Test
  def comment =
    assertEquals("<!-- thissa comment -->", <!-- thissa comment --> toString)

  @Test
  def weirdElem =
    assertEquals("<?this is a pi foo bar = && {{ ?>", <?this is a pi foo bar = && {{ ?> toString)

  @Test
  def escape =
    assertEquals("""
 &quot;Come, come again, whoever you are, come!
Heathen, fire worshipper or idolatrous, come!
Come even if you broke your penitence a hundred times,
Ours is the portal of hope, come as you are.&quot;
                              Mevlana Celaleddin Rumi""", <![CDATA[
 "Come, come again, whoever you are, come!
Heathen, fire worshipper or idolatrous, come!
Come even if you broke your penitence a hundred times,
Ours is the portal of hope, come as you are."
                              Mevlana Celaleddin Rumi]]> toString) // this guy will escaped, and rightly so

  @Test
  def unparsed2 = {
    object myBreak extends scala.xml.Unparsed("<br />")
    assertEquals("<foo><br /></foo>", <foo>{ myBreak }</foo> toString) // shows use of unparsed
  }

  @Test
  def justDontFail = {
    <x:foo xmlns:x="gaga"/> match {
      case scala.xml.QNode("gaga", "foo", md, child @ _*) =>
    }

    <x:foo xmlns:x="gaga"/> match {
      case scala.xml.Node("foo", md, child @ _*) =>
    }
  }

}
