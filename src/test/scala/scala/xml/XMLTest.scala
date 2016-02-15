package scala.xml

import language.postfixOps

import org.junit.{Test => UnitTest}
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import scala.xml.parsing.ConstructingParser
import java.io.StringWriter
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.StringReader
import scala.collection.Iterable
import scala.xml.Utility.sort

object XMLTest {
  val e: scala.xml.MetaData = Null //Node.NoAttributes
  val sc: scala.xml.NamespaceBinding = TopScope
}

class XMLTest {
  import XMLTest.{ e, sc }

  @UnitTest
  def nodeSeq: Unit = {
    val p = <foo>
              <bar gt='ga' value="3"/>
              <baz bazValue="8"/>
              <bar value="5" gi='go'/>
            </foo>

    val pelems_1 = for (x <- p \ "bar"; y <- p \ "baz") yield {
      Text(x.attributes("value").toString + y.attributes("bazValue").toString + "!")
    };

    val pelems_2 = NodeSeq.fromSeq(List(Text("38!"), Text("58!")));
    assertTrue(pelems_1 sameElements pelems_2)
    assertTrue(Text("8") sameElements (p \\ "@bazValue"))
  }

  @UnitTest
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
      val actual = for (t @ <book><title>Blabla</title></book> <- NodeSeq.fromSeq(books.child).toList)
        yield t
      val expected = List(<book><title>Blabla</title></book>)
      assertEquals(expected, actual)
    }

  }

  @UnitTest
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

  @UnitTest
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

  @UnitTest
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

  def Elem(prefix: String, label: String, attributes: MetaData, scope: NamespaceBinding, child: Node*): Elem =
    scala.xml.Elem.apply(prefix, label, attributes, scope, minimizeEmpty = true, child: _*)

  lazy val parsedxml1 = XML.load(new InputSource(new StringReader("<hello><world/></hello>")))
  lazy val parsedxml11 = XML.load(new InputSource(new StringReader("<hello><world/></hello>")))
  val xmlFile2 = "<bib><book><author>Peter Buneman</author><author>Dan Suciu</author><title>Data on ze web</title></book><book><author>John Mitchell</author><title>Foundations of Programming Languages</title></book></bib>";
  lazy val parsedxml2 = XML.load(new InputSource(new StringReader(xmlFile2)))

  @UnitTest
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

  @UnitTest
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

  @UnitTest
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
      ((NodeSeq.fromSeq(List(parsedxml2))) \\ "_") sameElements List(
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

  @UnitTest
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

  @UnitTest
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

  @UnitTest
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

  @UnitTest
  def XmlEx = {
    assertTrue((ax \ "@foo") xml_== "bar") // uses NodeSeq.view!
    assertTrue((ax \ "@foo") xml_== xml.Text("bar")) // dto.
    assertTrue((bx \ "@foo") xml_== "bar&x") // dto.
    assertTrue((bx \ "@foo") xml_sameElements List(xml.Text("bar&x")))
    assertTrue("<hello foo=\"bar&amp;x\"></hello>" == bx.toString)
  }

  @UnitTest
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

  @UnitTest
  def comment =
    assertEquals("<!-- thissa comment -->", <!-- thissa comment --> toString)

  @UnitTest
  def weirdElem =
    assertEquals("<?this is a pi foo bar = && {{ ?>", <?this is a pi foo bar = && {{ ?> toString)

  @UnitTest
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

  @UnitTest
  def unparsed2 = {
    object myBreak extends scala.xml.Unparsed("<br />")
    assertEquals("<foo><br /></foo>", <foo>{ myBreak }</foo> toString) // shows use of unparsed
  }

  @UnitTest
  def justDontFail = {
    <x:foo xmlns:x="gaga"/> match {
      case scala.xml.QNode("gaga", "foo", md, child @ _*) =>
    }

    <x:foo xmlns:x="gaga"/> match {
      case scala.xml.Node("foo", md, child @ _*) =>
    }
  }

  @UnitTest
  def ioPosition = {
    val out = new ByteArrayOutputStream
    Console.withErr(out) {
      Console.withOut(out) {
        try {
          xml.parsing.ConstructingParser.fromSource(io.Source.fromString("<foo>"), false).document()
        } catch {
          case e: Exception => println(e.getMessage)
        }
      }
    }
    out.flush()
    assertEquals(
      """:1:5: '/' expected instead of ' '    ^
:1:5: name expected, but char ' ' cannot start a name    ^
expected closing tag of foo
""", out.toString)
  }

  def f(s: String) = {
    <entry>
      {
        for (item <- s split ',') yield <elem>{ item }</elem>
      }
    </entry>
  }

  @UnitTest
  def nodeBuffer =
    assertEquals(
      """<entry>
      <elem>a</elem><elem>b</elem><elem>c</elem>
    </entry>""", f("a,b,c") toString)

  object Serialize {
    @throws(classOf[java.io.IOException])
    def write[A](o: A): Array[Byte] = {
      val ba = new ByteArrayOutputStream(512)
      val out = new java.io.ObjectOutputStream(ba)
      out.writeObject(o)
      out.close()
      ba.toByteArray()
    }
    @throws(classOf[java.io.IOException])
    @throws(classOf[ClassNotFoundException])
    def read[A](buffer: Array[Byte]): A = {
      val in =
        new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(buffer))
      in.readObject().asInstanceOf[A]
    }
    def check[A, B](x: A, y: B) {
      // println("x = " + x)
      // println("y = " + y)
      // println("x equals y: " + (x equals y) + ", y equals x: " + (y equals x))
      assertTrue((x equals y) && (y equals x))
      // println()
    }
  }

  import Serialize._

  @UnitTest
  def serializeAttribute = {
    // Attribute
    val a1 = new PrefixedAttribute("xml", "src", Text("hello"), Null)
    val _a1: Attribute = read(write(a1))
    check(a1, _a1)
  }

  @UnitTest
  def serializeDocument = {
    // Document
    val d1 = new Document
    d1.docElem = <title></title>
    d1.encoding = Some("UTF-8")
    val _d1: Document = read(write(d1))
    check(d1, _d1)
  }

  @UnitTest
  def serializeElem = {
    // Elem
    val e1 = <html><title>title</title><body></body></html>;
    val _e1: Elem = read(write(e1))
    check(e1, _e1)
  }

  @UnitTest
  def serializeComplex = {
    case class Person(name: String, age: Int)
    class AddressBook(a: Person*) {
      private val people: List[Person] = a.toList
      def toXHTML =
        <table cellpadding="2" cellspacing="0">
          <tr>
            <th>Last Name</th>
            <th>First Name</th>
          </tr>
          {
            for (p <- people) yield <tr>
                                      <td> { p.name } </td>
                                      <td> { p.age.toString() } </td>
                                    </tr>
          }
        </table>;
    }

    val people = new AddressBook(
      Person("Tom", 20),
      Person("Bob", 22),
      Person("James", 19))

    val e2 =
      <html>
        <body>
          { people.toXHTML }
        </body>
      </html>;
    val _e2: Elem = read(write(e2))
    check(e2, _e2)
  }

  // t-486
  def wsdlTemplate1(serviceName: String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ "target1" }>
    </wsdl:definitions>;

  def wsdlTemplate2(serviceName: String, targetNamespace: String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ targetNamespace }>
    </wsdl:definitions>;

  def wsdlTemplate3(serviceName: String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ new _root_.scala.xml.Text("target3") }>
    </wsdl:definitions>;

  def wsdlTemplate4(serviceName: String, targetNamespace: () => String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ targetNamespace() }>
    </wsdl:definitions>;

  @UnitTest
  def wsdl = {
    assertEquals("""<wsdl:definitions name="service1" xmlns:tns="target1">
    </wsdl:definitions>""", wsdlTemplate1("service1") toString)
    assertEquals("""<wsdl:definitions name="service2" xmlns:tns="target2">
    </wsdl:definitions>""", wsdlTemplate2("service2", "target2") toString)
    assertEquals("""<wsdl:definitions name="service3" xmlns:tns="target3">
    </wsdl:definitions>""", wsdlTemplate3("service3") toString)
    assertEquals("""<wsdl:definitions name="service4" xmlns:tns="target4">
    </wsdl:definitions>""", wsdlTemplate4("service4", () => "target4") toString)
  }

  @UnitTest
  def t0663 = {
    val src = scala.io.Source.fromString("<?xml version='1.0' encoding='UTF-8'?><feed/>")
    val parser = xml.parsing.ConstructingParser.fromSource(src, true)
    assertEquals("<feed/>", parser.document toString)
  }

  @UnitTest
  def t1079 = assertFalse(<t user:tag=""/> == <t user:tag="X"/>)

  import dtd.{ DocType, PublicID }

  @UnitTest
  def t1620 = {
    val dt = DocType("foo", PublicID("-//Foo Corp//DTD 1.0//EN", "foo.dtd"), Seq())
    var pw = new StringWriter()
    XML.write(pw, <foo/>, "utf-8", true, dt)
    pw.flush()
    assertEquals("""<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE foo PUBLIC "-//Foo Corp//DTD 1.0//EN" "foo.dtd">
<foo/>""", pw.toString)

    pw = new StringWriter()
    val dt2 = DocType("foo", PublicID("-//Foo Corp//DTD 1.0//EN", null), Seq())
    XML.write(pw, <foo/>, "utf-8", true, dt2)
    pw.flush()
    assertEquals("""<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE foo PUBLIC "-//Foo Corp//DTD 1.0//EN">
<foo/>""", pw.toString)
  }

  @UnitTest
  def t1773 = {
    val xs = List(
      <a></a>,
      <a/>,
      <a>{ xml.NodeSeq.Empty }</a>,
      <a>{ "" }</a>,
      <a>{ if (true) "" else "I like turtles" }</a>)

    for (x1 <- xs; x2 <- xs) assertTrue(x1 xml_== x2)
  }

  @UnitTest
  def t2354: Unit = {
    val xml_good = "<title><![CDATA[Hello [tag]]]></title>"
    val xml_bad = "<title><![CDATA[Hello [tag] ]]></title>"

    val parser1 = ConstructingParser.fromSource(io.Source.fromString(xml_good), false)
    val parser2 = ConstructingParser.fromSource(io.Source.fromString(xml_bad), false)

    parser1.document
    parser2.document
  }

  @UnitTest
  def t2771 = {
    val xml1 = <root xmlns:ns="nsUri" ns:at="rootVal"><sub ns:at="subVal"/></root>
    val xml2 = scala.xml.XML.loadString("""<root xmlns:ns="nsUri" ns:at="rootVal"><sub ns:at="subVal"/></root>""")

    def backslashSearch(x: xml.Elem) = "root:-" + (x \ "@{nsUri}at") + "-sub:-" + (x \ "sub" \ "@{nsUri}at") + "-"

    assertEquals("root:-rootVal-sub:-subVal-", backslashSearch(xml1) toString)
    assertEquals("root:-rootVal-sub:-subVal-", backslashSearch(xml2) toString)
  }

  @UnitTest
  def t3886 = {
    assertTrue(<k a="1" b="2"/> == <k a="1" b="2"/>)
    assertTrue(<k a="1" b="2"/> != <k a="1" b="3"/>)
    assertTrue(<k a="1" b="2"/> != <k a="2" b="2"/>)

    assertTrue(<k a="1" b="2"/> != <k/>)
    assertTrue(<k a="1" b="2"/> != <k a="1"/>)
    assertTrue(<k a="1" b="2"/> != <k b="2"/>)
  }

  @UnitTest
  def t4387 = {
    import XML.loadString
    def mkElem(arg: String) = <foo a="1" b="2" c="3" d="4" e={ arg }/>

    val x1 = mkElem("5")
    val x2 = mkElem("50")

    assertEquals(x1, loadString("" + x1))
    assertTrue(x2 != loadString("" + x1))
  }

  @UnitTest
  def t5052 {
    assertTrue(<elem attr={ null: String }/> xml_== <elem/>)
    assertTrue(<elem attr={ None }/> xml_== <elem/>)
    assertTrue(<elem/> xml_== <elem attr={ null: String }/>)
    assertTrue(<elem/> xml_== <elem attr={ None }/>)
  }

  @UnitTest
  def t5115 = {
    def assertHonorsIterableContract(i: Iterable[_]) = assertEquals(i.size, i.iterator.size)

    assertHonorsIterableContract(<a/>.attributes)
    assertHonorsIterableContract(<a x=""/>.attributes)
    assertHonorsIterableContract(<a y={ None }/>.attributes)
    assertHonorsIterableContract(<a y={ None } x=""/>.attributes)
    assertHonorsIterableContract(<a a="" y={ None }/>.attributes)
    assertHonorsIterableContract(<a y={ null: String }/>.attributes)
    assertHonorsIterableContract(<a y={ null: String } x=""/>.attributes)
    assertHonorsIterableContract(<a a="" y={ null: String }/>.attributes)
  }

  @UnitTest
  def t5843 {
    val foo = scala.xml.Attribute(null, "foo", "1", scala.xml.Null)
    val bar = scala.xml.Attribute(null, "bar", "2", foo)
    val ns = scala.xml.NamespaceBinding(null, "uri", scala.xml.TopScope)

    assertEquals(""" foo="1"""", foo toString)
    assertEquals(null, scala.xml.TopScope.getURI(foo.pre))
    assertEquals(""" bar="2"""", bar remove "foo" toString)
    assertEquals(""" foo="1"""", bar remove "bar" toString)
    assertEquals(""" bar="2"""", bar remove (null, scala.xml.TopScope, "foo") toString)
    assertEquals(""" foo="1"""", bar remove (null, scala.xml.TopScope, "bar") toString)
    assertEquals(""" bar="2" foo="1"""", bar toString)
    assertEquals(""" bar="2" foo="1"""", bar remove (null, ns, "foo") toString)
    assertEquals(""" bar="2" foo="1"""", bar remove (null, ns, "bar") toString)
  }

  @UnitTest
  def t6939 = {
    val foo = <x:foo xmlns:x="http://foo.com/"><x:bar xmlns:x="http://bar.com/"><x:baz/></x:bar></x:foo>
    assertTrue(foo.child.head.scope.toString == """ xmlns:x="http://bar.com/"""")

    val fooDefault = <foo xmlns="http://foo.com/"><bar xmlns="http://bar.com/"><baz/></bar></foo>
    assertTrue(fooDefault.child.head.scope.toString == """ xmlns="http://bar.com/"""")

    val foo2 = scala.xml.XML.loadString("""<x:foo xmlns:x="http://foo.com/"><x:bar xmlns:x="http://bar.com/"><x:baz/></x:bar></x:foo>""")
    assertTrue(foo2.child.head.scope.toString == """ xmlns:x="http://bar.com/"""")

    val foo2Default = scala.xml.XML.loadString("""<foo xmlns="http://foo.com/"><bar xmlns="http://bar.com/"><baz/></bar></foo>""")
    assertTrue(foo2Default.child.head.scope.toString == """ xmlns="http://bar.com/"""")
  }

  @UnitTest
  def t7074 {
    assertEquals("""<a/>""", sort(<a/>) toString)
    assertEquals("""<a b="2" c="3" d="1"/>""", sort(<a d="1" b="2" c="3"/>) toString)
    assertEquals("""<a b="2" c="4" d="1" e="3" f="5"/>""", sort(<a d="1" b="2" e="3" c="4" f="5"/>) toString)
    assertEquals("""<a b="5" c="4" d="3" e="2" f="1"/>""", sort(<a f="1" e="2" d="3" c="4" b="5"/>) toString)
    assertEquals("""<a b="1" c="2" d="3" e="4" f="5"/>""", sort(<a b="1" c="2" d="3" e="4" f="5"/>) toString)
    assertEquals("""<a a:b="2" a:c="3" a:d="1"/>""", sort(<a a:d="1" a:b="2" a:c="3"/>) toString)
    assertEquals("""<a a:b="2" a:c="4" a:d="1" a:e="3" a:f="5"/>""", sort(<a a:d="1" a:b="2" a:e="3" a:c="4" a:f="5"/>) toString)
    assertEquals("""<a a:b="5" a:c="4" a:d="3" a:e="2" a:f="1"/>""", sort(<a a:f="1" a:e="2" a:d="3" a:c="4" a:b="5"/>) toString)
    assertEquals("""<a a:b="1" a:c="2" a:d="3" a:e="4" a:f="5"/>""", sort(<a a:b="1" a:c="2" a:d="3" a:e="4" a:f="5"/>) toString)
  }

  @UnitTest
  def attributes = {
    val noAttr = <t/>
    val attrNull = <t a={ null: String }/>
    val attrNone = <t a={ None: Option[Seq[Node]] }/>
    val preAttrNull = <t p:a={ null: String }/>
    val preAttrNone = <t p:a={ None: Option[Seq[Node]] }/>
    assertEquals(noAttr, attrNull)
    assertEquals(noAttr, attrNone)
    assertEquals(noAttr, preAttrNull)
    assertEquals(noAttr, preAttrNone)

    val xml1 = <t b="1" d="2"/>
    val xml2 = <t a={ null: String } p:a={ null: String } b="1" c={ null: String } d="2"/>
    val xml3 = <t b="1" c={ null: String } d="2" a={ null: String } p:a={ null: String }/>
    assertEquals(xml1, xml2)
    assertEquals(xml1, xml3)

    assertEquals("""<t/>""", noAttr toString)
    assertEquals("""<t/>""", attrNull toString)
    assertEquals("""<t/>""", attrNone toString)
    assertEquals("""<t/>""", preAttrNull toString)
    assertEquals("""<t/>""", preAttrNone toString)
    assertEquals("""<t b="1" d="2"/>""", xml1 toString)
    assertEquals("""<t b="1" d="2"/>""", xml2 toString)
    assertEquals("""<t b="1" d="2"/>""", xml3 toString)

    // Check if attribute order is retained
    assertEquals("""<t a="1" d="2"/>""", <t a="1" d="2"/> toString)
    assertEquals("""<t b="1" d="2"/>""", <t b="1" d="2"/> toString)
    assertEquals("""<t a="1" b="2" c="3"/>""", <t a="1" b="2" c="3"/> toString)
    assertEquals("""<t g="1" e="2" p:a="3" f:e="4" mgruhu:ji="5"/>""", <t g="1" e="2" p:a="3" f:e="4" mgruhu:ji="5"/> toString)
  }

  import java.io.{ Console => _, _ }
  import scala.io._
  import scala.xml.parsing._
  @UnitTest
  def dontLoop: Unit = {
    val xml = "<!DOCTYPE xmeml SYSTEM> <xmeml> <sequence> </sequence> </xmeml> "
    val sink = new PrintStream(new ByteArrayOutputStream())
    (Console withOut sink) {
      (Console withErr sink) {
        ConstructingParser.fromSource((io.Source fromString xml), true).document.docElem
      }
    }
  }

  @UnitTest
  def issue28: Unit = {
    val x = <x:foo xmlns:x="gaga"/>
    // val ns = new NamespaceBinding("x", "gaga", sc)
    // val x = Elem("x", "foo", e, ns)
    val pp = new xml.PrettyPrinter(80, 2)
    // This assertion passed
    assertEquals("""<x:foo xmlns:x="gaga"/>""", x.toString)
    // This was the bug, producing an errant xmlns attribute
    assertEquals("""<x:foo xmlns:x="gaga"/>""", pp.format(x))
  }

  @UnitTest( expected = classOf[scala.xml.SAXParseException] )
  def issue35: Unit = {
    val broken = "<broken attribute='is truncated"
    XML.loadString(broken)
  }

  @UnitTest
  def nodeSeqNs: Unit = {
    val x = {
      <x:foo xmlns:x="abc"/><y:bar xmlns:y="def"/>
    }
    val pp = new PrettyPrinter(80, 2)
    val expected = """<x:foo xmlns:x="abc"/><y:bar xmlns:y="def"/>"""
    assertEquals(expected, pp.formatNodes(x))
  }

  @UnitTest
  def nodeStringBuilder: Unit = {
    val x = {
        <x:foo xmlns:x="abc"/>
    }
    val pp = new PrettyPrinter(80, 2)
    val expected = """<x:foo xmlns:x="abc"/>"""
    val sb = new StringBuilder
    pp.format(x, sb)
    assertEquals(expected, sb.toString)
  }
}
