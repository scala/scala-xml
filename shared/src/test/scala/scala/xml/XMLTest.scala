package scala.xml

import org.junit.{Test => UnitTest}
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import java.io.StringWriter
import scala.collection.Iterable
import scala.collection.Seq
import scala.xml.dtd.{DocType, PublicID}
import scala.xml.Utility.sort

object XMLTest {
  val e: MetaData = Null //Node.NoAttributes
  val sc: NamespaceBinding = TopScope
}

class XMLTest {
  @UnitTest
  def nodeSeq(): Unit = {
    val p: Elem = <foo>
                    <bar gt='ga' value="3"/>
                    <baz bazValue="8"/>
                    <bar value="5" gi='go'/>
                  </foo>

    val pelems_1: NodeSeq = for (x <- p \ "bar"; y <- p \ "baz") yield {
      val value = x.attributes("value")
      val bazValue = y.attributes("bazValue")
      Text(s"$value$bazValue!")
    }

    val pelems_2: NodeSeq = NodeSeq.fromSeq(List(Text("38!"), Text("58!")))
    assertTrue(pelems_1.sameElements(pelems_2))
    assertTrue(Text("8").sameElements(p \\ "@bazValue"))
  }

  @UnitTest
  def queryBooks(): Unit = {
    val books: Elem =
      <bks>
        <book><title>Blabla</title></book>
        <book><title>Blubabla</title></book>
        <book><title>Baaaaaaalabla</title></book>
      </bks>

    val reviews: Elem =
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
      </reviews>

    val results1: String = new PrettyPrinter(80, 5).formatNodes(
      for {
        t <- books \\ "title"
        r <- reviews \\ "entry" if (r \ "title") xml_== t
      } yield <result>
                { t }
                { r \ "remarks" }
              </result>)
    val results1Expected: String = """<result>
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
      val actual: List[Node] = for (case t @ <book><title>Blabla</title></book> <- NodeSeq.fromSeq(books.child).toList)
        yield t
      val expected: List[Elem] = List(<book><title>Blabla</title></book>)
      assertEquals(expected, actual)
    }
  }

  @UnitTest
  def queryPhoneBook(): Unit = {
    val phoneBook: Elem =
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
      </phonebook>

    val addrBook: Elem =
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
      </addrbook>

    val actual: String = new PrettyPrinter(80, 5).formatNodes(
      for {
        t <- addrBook \\ "entry"
        r <- phoneBook \\ "entry" if (t \ "name") xml_== (r \ "name")
      } yield <result>
                { t.child }
                { r \ "phone" }
              </result>)
    val expected: String =
     """|<result>
        |     <name>John</name>
        |     <street> Elm Street</street>
        |     <city>Dolphin City</city>
        |     <phone where="work"> +41 21 693 68 67</phone>
        |     <phone where="mobile">+41 79 602 23 23</phone>
        |</result>""".stripMargin
    assertEquals(expected, actual)
  }

  @UnitTest(expected=classOf[IllegalArgumentException])
  def failEmptyStringChildren(): Unit = {
    <x/> \ ""
  }

  @UnitTest(expected=classOf[IllegalArgumentException])
  def failEmptyStringDescendants(): Unit = {
    <x/> \\ ""
  }

  @UnitTest
  def namespaces(): Unit = {
    val cuckoo: Elem = <cuckoo xmlns="http://cuckoo.com">
                         <foo/>
                         <bar/>
                       </cuckoo>
    assertEquals("http://cuckoo.com", cuckoo.namespace)
    for (n <- cuckoo \ "_") {
      assertEquals("http://cuckoo.com", n.namespace)
    }
  }

  @UnitTest
  def namespacesWithNestedXmls(): Unit = {
    val foo: Elem = <f:foo xmlns:f="fooUrl"></f:foo>
    val bar: Elem = <b:bar xmlns:b="barUrl">{foo}</b:bar>
    val expected: String = """<b:bar xmlns:b="barUrl"><f:foo xmlns:f="fooUrl"></f:foo></b:bar>"""
    val actual: String = bar.toString
    assertEquals(expected, actual)
  }

  def Elem(prefix: String, label: String, attributes: MetaData, scope: NamespaceBinding, child: Node*): Elem =
    scala.xml.Elem.apply(prefix, label, attributes, scope, minimizeEmpty = true, child: _*)

  @UnitTest
  def groupNode(): Unit = {
    val zx1: Node = Group { <a/><b/><c/> }
    val zy1: Node = <f>{ zx1 }</f>
    assertEquals("<f><a/><b/><c/></f>", zy1.toString)

    assertEquals("<a/><f><a/><b/><c/></f><a/><b/><c/>",
      Group { List(<a/>, zy1, zx1) }.toString)

    val zz1: Group = <xml:group><a/><b/><c/></xml:group>

    assertTrue(zx1 xml_== zz1)
    assertTrue(zz1.length == 3)
  }

  @UnitTest
  def dodgyNamespace(): Unit = {
    val x: Elem = <flog xmlns:ee="http://ee.com"><foo xmlns:dog="http://dog.com"><dog:cat/></foo></flog>
    assertTrue(x.toString.matches(".*xmlns:dog=\"http://dog.com\".*"))
  }

  val ax: Elem = <hello foo="bar" x:foo="baz" xmlns:x="the namespace from outer space">
                   <world/>
                 </hello>

  val cx: Elem = <z:hello foo="bar" xmlns:z="z" x:foo="baz" xmlns:x="the namespace from outer space">
                   crazy text world
                 </z:hello>

  val bx: Elem = <hello foo="bar&amp;x"></hello>

  @UnitTest
  def XmlEx(): Unit = {
    assertTrue((ax \ "@foo") xml_== "bar") // uses NodeSeq.view!
    assertTrue((ax \ "@foo") xml_== Text("bar")) // dto.
    assertTrue((bx \ "@foo") xml_== "bar&x") // dto.
    assertTrue((bx \ "@foo") xml_sameElements List(Text("bar&x")))
    assertTrue("<hello foo=\"bar&amp;x\"></hello>" == bx.toString)
  }

  @UnitTest
  def XmlEy(): Unit = {
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
  def comment(): Unit =
    assertEquals("<!-- thissa comment -->", <!-- thissa comment -->.toString)

  @UnitTest
  def weirdElem(): Unit =
    assertEquals("<?this is a pi foo bar = && {{ ?>", <?this is a pi foo bar = && {{ ?>.toString)

  @UnitTest
  def escape(): Unit =
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
                              Mevlana Celaleddin Rumi]]>.toString) // this guy will escaped, and rightly so

  @UnitTest
  def unparsed2(): Unit = {
    object myBreak extends Unparsed("<br />")
    assertEquals("<foo><br /></foo>", <foo>{ myBreak }</foo>.toString) // shows use of unparsed
  }

  @UnitTest
  def justDontFail(): Unit = {
    <x:foo xmlns:x="gaga"/> match {
      case QNode("gaga", "foo", md, child @ _*) =>
    }

    <x:foo xmlns:x="gaga"/> match {
      case Node("foo", md, child @ _*) =>
    }
  }

  def f(s: String): Elem = {
    <entry>
      {
        for (item <- s split ',') yield <elem>{ item }</elem>
      }
    </entry>
  }

  @UnitTest
  def nodeBuffer(): Unit =
    assertEquals(
      """<entry>
      <elem>a</elem><elem>b</elem><elem>c</elem>
    </entry>""", f("a,b,c").toString)

  // t-486
  def wsdlTemplate1(serviceName: String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ "target1" }>
    </wsdl:definitions>

  def wsdlTemplate2(serviceName: String, targetNamespace: String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ targetNamespace }>
    </wsdl:definitions>

  def wsdlTemplate4(serviceName: String, targetNamespace: () => String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ targetNamespace() }>
    </wsdl:definitions>

  @UnitTest
  def wsdl(): Unit = {
    assertEquals("""<wsdl:definitions name="service1" xmlns:tns="target1">
    </wsdl:definitions>""", wsdlTemplate1("service1").toString)
    assertEquals("""<wsdl:definitions name="service2" xmlns:tns="target2">
    </wsdl:definitions>""", wsdlTemplate2("service2", "target2").toString)
    assertEquals("""<wsdl:definitions name="service4" xmlns:tns="target4">
    </wsdl:definitions>""", wsdlTemplate4("service4", () => "target4").toString)
  }

  @UnitTest
  def t547(): Unit = {
    // ambiguous toString problem from #547
    val atom: Atom[Unit] = new Atom(())
    assertEquals(().toString, atom.toString)
  }

  @UnitTest
  def t1079(): Unit = assertFalse(<t user:tag=""/> == <t user:tag="X"/>)

  @UnitTest
  def t1620(): Unit = {
    val dt: DocType = DocType("foo", PublicID("-//Foo Corp//DTD 1.0//EN", "foo.dtd"), Seq())
    var pw: StringWriter = new StringWriter()
    XML.write(pw, <foo/>, "utf-8", xmlDecl = true, dt)
    pw.flush()
    assertEquals("""<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE foo PUBLIC "-//Foo Corp//DTD 1.0//EN" "foo.dtd">
<foo/>""", pw.toString)

    pw = new StringWriter()
    val dt2: DocType = DocType("foo", PublicID("-//Foo Corp//DTD 1.0//EN", null), Seq())
    XML.write(pw, <foo/>, "utf-8", xmlDecl = true, dt2)
    pw.flush()
    assertEquals("""<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE foo PUBLIC "-//Foo Corp//DTD 1.0//EN">
<foo/>""", pw.toString)
  }

  @UnitTest
  def t1773(): Unit = {
    val xs: List[Elem] = List(
      <a></a>,
      <a/>,
      <a>{ NodeSeq.Empty }</a>,
      <a>{ "" }</a>,
      <a>{ if (true) "" else "I like turtles" }</a>)

    for (x1 <- xs; x2 <- xs) assertTrue(x1 xml_== x2)
  }

  @UnitTest
  def t3886(): Unit = {
    assertTrue(<k a="1" b="2"/> == <k a="1" b="2"/>)
    assertTrue(<k a="1" b="2"/> != <k a="1" b="3"/>)
    assertTrue(<k a="1" b="2"/> != <k a="2" b="2"/>)

    assertTrue(<k a="1" b="2"/> != <k/>)
    assertTrue(<k a="1" b="2"/> != <k a="1"/>)
    assertTrue(<k a="1" b="2"/> != <k b="2"/>)
  }

  @UnitTest
  def t4124(): Unit = {
    val body: Node = <elem>hi</elem>
    assertEquals("hi", ((body: AnyRef, "foo"): @unchecked) match {
      case (node: Node, "bar")        => "bye"
      case (ser: Serializable, "foo") => "hi"
    })

    assertEquals("hi", ((body, "foo"): @unchecked) match {
      case (node: Node, "bar")        => "bye"
      case (ser: Serializable, "foo") => "hi"
    })

    assertEquals("bye", ((body: AnyRef, "foo"): @unchecked) match {
      case (node: Node, "foo")        => "bye"
      case (ser: Serializable, "foo") => "hi"
    })

    assertEquals("bye", ((body: AnyRef, "foo"): @unchecked) match {
      case (node: Node, "foo")        => "bye"
      case (ser: Serializable, "foo") => "hi"
    })
  }

  @UnitTest
  def t5052(): Unit = {
    assertTrue(<elem attr={ null: String }/> xml_== <elem/>)
    assertTrue(<elem attr={ None }/> xml_== <elem/>)
    assertTrue(<elem/> xml_== <elem attr={ null: String }/>)
    assertTrue(<elem/> xml_== <elem attr={ None }/>)
  }

  @UnitTest
  def t5115(): Unit = {
    def assertHonorsIterableContract(i: Iterable[?]): Unit = assertEquals(i.size.toLong, i.iterator.size.toLong)

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
  def t5843(): Unit = {
    val foo: Attribute = Attribute(null, "foo", "1", Null)
    val bar: Attribute = Attribute(null, "bar", "2", foo)
    val ns: NamespaceBinding = NamespaceBinding(null, "uri", TopScope)

    assertEquals(""" foo="1"""", foo.toString)
    assertEquals(null, TopScope.getURI(foo.pre))
    assertEquals(""" bar="2"""", bar.remove("foo").toString)
    assertEquals(""" foo="1"""", bar.remove("bar").toString)
    assertEquals(""" bar="2"""", bar.remove(null, TopScope, "foo").toString)
    assertEquals(""" foo="1"""", bar.remove(null, TopScope, "bar").toString)
    assertEquals(""" bar="2" foo="1"""", bar.toString)
    assertEquals(""" bar="2" foo="1"""", bar.remove(null, ns, "foo").toString)
    assertEquals(""" bar="2" foo="1"""", bar.remove(null, ns, "bar").toString)
  }

  @UnitTest
  def t7074(): Unit = {
    assertEquals("""<a/>""", sort(<a/>).toString)
    assertEquals("""<a b="2" c="3" d="1"/>""", sort(<a d="1" b="2" c="3"/>).toString)
    assertEquals("""<a b="2" c="4" d="1" e="3" f="5"/>""", sort(<a d="1" b="2" e="3" c="4" f="5"/>).toString)
    assertEquals("""<a b="5" c="4" d="3" e="2" f="1"/>""", sort(<a f="1" e="2" d="3" c="4" b="5"/>).toString)
    assertEquals("""<a b="1" c="2" d="3" e="4" f="5"/>""", sort(<a b="1" c="2" d="3" e="4" f="5"/>).toString)
    assertEquals("""<a a:b="2" a:c="3" a:d="1"/>""", sort(<a a:d="1" a:b="2" a:c="3"/>).toString)
    assertEquals("""<a a:b="2" a:c="4" a:d="1" a:e="3" a:f="5"/>""", sort(<a a:d="1" a:b="2" a:e="3" a:c="4" a:f="5"/>).toString)
    assertEquals("""<a a:b="5" a:c="4" a:d="3" a:e="2" a:f="1"/>""", sort(<a a:f="1" a:e="2" a:d="3" a:c="4" a:b="5"/>).toString)
    assertEquals("""<a a:b="1" a:c="2" a:d="3" a:e="4" a:f="5"/>""", sort(<a a:b="1" a:c="2" a:d="3" a:e="4" a:f="5"/>).toString)
  }

  @UnitTest
  def attributes(): Unit = {
    val noAttr: Elem = <t/>
    val attrNull: Elem = <t a={ null: String }/>
    val attrNone: Elem = <t a={ None: Option[Seq[Node]] }/>
    val preAttrNull: Elem = <t p:a={ null: String }/>
    val preAttrNone: Elem = <t p:a={ None: Option[Seq[Node]] }/>
    assertEquals(noAttr, attrNull)
    assertEquals(noAttr, attrNone)
    assertEquals(noAttr, preAttrNull)
    assertEquals(noAttr, preAttrNone)

    val xml1: Elem = <t b="1" d="2"/>
    val xml2: Elem = <t a={ null: String } p:a={ null: String } b="1" c={ null: String } d="2"/>
    val xml3: Elem = <t b="1" c={ null: String } d="2" a={ null: String } p:a={ null: String }/>
    assertEquals(xml1, xml2)
    assertEquals(xml1, xml3)

    assertEquals("""<t/>""", noAttr.toString)
    assertEquals("""<t/>""", attrNull.toString)
    assertEquals("""<t/>""", attrNone.toString)
    assertEquals("""<t/>""", preAttrNull.toString)
    assertEquals("""<t/>""", preAttrNone.toString)
    assertEquals("""<t b="1" d="2"/>""", xml1.toString)
    assertEquals("""<t b="1" d="2"/>""", xml2.toString)
    assertEquals("""<t b="1" d="2"/>""", xml3.toString)

    // Check if attribute order is retained
    assertEquals("""<t a="1" d="2"/>""", <t a="1" d="2"/>.toString)
    assertEquals("""<t b="1" d="2"/>""", <t b="1" d="2"/>.toString)
    assertEquals("""<t a="1" b="2" c="3"/>""", <t a="1" b="2" c="3"/>.toString)
    assertEquals("""<t g="1" e="2" p:a="3" f:e="4" mgruhu:ji="5"/>""", <t g="1" e="2" p:a="3" f:e="4" mgruhu:ji="5"/>.toString)
  }

  @UnitTest
  def issue28(): Unit = {
    val x: Elem = <x:foo xmlns:x="gaga"/>
    // val ns = new NamespaceBinding("x", "gaga", sc)
    // val x = Elem("x", "foo", e, ns)
    val pp: PrettyPrinter = new PrettyPrinter(80, 2)
    // This assertion passed
    assertEquals("""<x:foo xmlns:x="gaga"/>""", x.toString)
    // This was the bug, producing an errant xmlns attribute
    assertEquals("""<x:foo xmlns:x="gaga"/>""", pp.format(x))
  }

  @UnitTest
  def nodeSeqNs(): Unit = {
    val x: NodeBuffer = {
      <x:foo xmlns:x="abc"/><y:bar xmlns:y="def"/>
    }
    val pp: PrettyPrinter = new PrettyPrinter(80, 2)
    val expected: String = """<x:foo xmlns:x="abc"/><y:bar xmlns:y="def"/>"""
    assertEquals(expected, pp.formatNodes(x))
  }

  @UnitTest
  def nodeStringBuilder(): Unit = {
    val x: Elem = {
        <x:foo xmlns:x="abc"/>
    }
    val pp: PrettyPrinter = new PrettyPrinter(80, 2)
    val expected: String = """<x:foo xmlns:x="abc"/>"""
    val sb: StringBuilder = new StringBuilder
    pp.format(x, sb)
    assertEquals(expected, sb.toString)
  }

  @UnitTest
  def i1976(): Unit = {
    val node: Elem = <node>{ "whatever " }</node>
    assertEquals("whatever ", node.child.text) // implicit seqToNodeSeq
  }

  @UnitTest
  def i6547(): Unit = {
    <foo a="hello &name; aaa"/>
  }
}
