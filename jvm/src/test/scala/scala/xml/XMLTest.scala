package scala.xml

import language.postfixOps

import org.junit.{Test => UnitTest}
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import java.io.StringWriter
import java.io.ByteArrayOutputStream
import java.io.StringReader
import scala.xml.dtd.{DocType, PublicID}
import scala.xml.parsing.ConstructingParser
import scala.xml.Utility.sort

object XMLTestJVM {
  val e: MetaData = Null //Node.NoAttributes
  val sc: NamespaceBinding = TopScope
}

class XMLTestJVM {
  import XMLTestJVM.{e, sc}

  def Elem(prefix: String, label: String, attributes: MetaData, scope: NamespaceBinding, child: Node*): Elem =
    scala.xml.Elem.apply(prefix, label, attributes, scope, minimizeEmpty = true, child: _*)

  lazy val parsedxml1: Elem = XML.load(new InputSource(new StringReader("<hello><world/></hello>")))
  lazy val parsedxml11: Elem = XML.load(new InputSource(new StringReader("<hello><world/></hello>")))
  val xmlFile2: String = "<bib><book><author>Peter Buneman</author><author>Dan Suciu</author><title>Data on ze web</title></book><book><author>John Mitchell</author><title>Foundations of Programming Languages</title></book></bib>"
  lazy val parsedxml2: Elem = XML.load(new InputSource(new StringReader(xmlFile2)))

  @UnitTest
  def equality(): Unit = {
    val c: Node = new Node {
      override def label: String = "hello"
      override def hashCode: Int =
        Utility.hashCode(prefix, label, this.attributes.hashCode(), scope.hashCode(), child)
      override def child: Seq[Node] = Elem(null, "world", e, sc)
      //def attributes = e
      override def text: String = ""
    }

    assertEquals(c, parsedxml11)
    assertEquals(parsedxml1, parsedxml11)
    assertTrue(List(parsedxml1) sameElements List(parsedxml11))
    assertTrue(Array(parsedxml1).toList sameElements List(parsedxml11))

    val x2: String = "<book><author>Peter Buneman</author><author>Dan Suciu</author><title>Data on ze web</title></book>"

    val i: InputSource = new InputSource(new StringReader(x2))
    val x2p: Elem = XML.load(i)

    assertEquals(Elem(null, "book", e, sc,
      Elem(null, "author", e, sc, Text("Peter Buneman")),
      Elem(null, "author", e, sc, Text("Dan Suciu")),
      Elem(null, "title", e, sc, Text("Data on ze web"))), x2p)
  }

  @UnitTest
  def xpath(): Unit = {
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
  def xpathDESCENDANTS(): Unit = {
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
      (parsedxml2 \\ "book") { (n: Node) => (n \ "title") xml_== "Data on ze web" }.toString)

    assertTrue(
      (NodeSeq.fromSeq(List(parsedxml2)) \\ "_") sameElements List(
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
  def unparsed(): Unit = {
    //    println("attribute value normalization")
    val xmlAttrValueNorm: String = "<personne id='p0003' nom='&#x015e;ahingz' />"

    {
      val isrcA: InputSource = new InputSource(new StringReader(xmlAttrValueNorm))
      val parsedxmlA: Elem = XML.load(isrcA)
      val c: Char = (parsedxmlA \ "@nom").text.charAt(0)
      assertTrue(c == '\u015e')
    }
    // buraq: if the following test fails with 'character x not allowed', it is
    //        related to the mutable variable in a closures in MarkupParser.parsecharref
    {
      val isr: scala.io.Source = scala.io.Source.fromString(xmlAttrValueNorm)
      val pxmlB: ConstructingParser = ConstructingParser.fromSource(isr, preserveWS = false)
      val parsedxmlB: NodeSeq = pxmlB.element(TopScope)
      val c: Char = (parsedxmlB \ "@nom").text.charAt(0)
      assertTrue(c == '\u015e')
    }

    // #60 test by round trip

    val p: ConstructingParser = ConstructingParser.fromSource(scala.io.Source.fromString("<foo bar:attr='&amp;'/>"), preserveWS = true)
    val n: Node = p.element(NamespaceBinding("bar", "BAR", TopScope))(0)
    assertTrue(n.attributes.get("BAR", n, "attr").nonEmpty)
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

  object Serialize {
    @throws(classOf[java.io.IOException])
    def write[A](o: A): Array[Byte] = {
      val ba: ByteArrayOutputStream = new ByteArrayOutputStream(512)
      val out: java.io.ObjectOutputStream = new java.io.ObjectOutputStream(ba)
      out.writeObject(o)
      out.close()
      ba.toByteArray
    }
    @throws(classOf[java.io.IOException])
    @throws(classOf[ClassNotFoundException])
    def read[A](buffer: Array[Byte]): A = {
      val in: java.io.ObjectInputStream =
        new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(buffer))
      in.readObject().asInstanceOf[A]
    }
    def check[A, B](x: A, y: B): Unit = {
      // println("x = " + x)
      // println("y = " + y)
      // println("x equals y: " + (x equals y) + ", y equals x: " + (y equals x))
      assertTrue((x equals y) && (y equals x))
      // println()
    }
  }

  @UnitTest
  def serializeAttribute(): Unit = {
    // Attribute
    val a1: PrefixedAttribute = new PrefixedAttribute("xml", "src", Text("hello"), Null)
    val _a1: Attribute = Serialize.read(Serialize.write(a1))
    Serialize.check(a1, _a1)
  }

  @UnitTest
  def serializeDocument(): Unit = {
    // Document
    val d1: Document = new Document
    d1.docElem = <title></title>
    d1.encoding = Some("UTF-8")
    val _d1: Document = Serialize.read(Serialize.write(d1))
    Serialize.check(d1, _d1)
  }

  @UnitTest
  def serializeElem(): Unit = {
    // Elem
    val e1: Elem = <html><title>title</title><body></body></html>
    val _e1: Elem = Serialize.read(Serialize.write(e1))
    Serialize.check(e1, _e1)
  }

  @UnitTest
  def serializeComplex(): Unit = {
    case class Person(name: String, age: Int)
    class AddressBook(a: Person*) {
      private val people: List[Person] = a.toList
      def toXHTML: Elem =
        <table cellpadding="2" cellspacing="0">
          <tr>
            <th>Last Name</th>
            <th>First Name</th>
          </tr>
          {
            for (p <- people) yield <tr>
                                      <td> { p.name } </td>
                                      <td> { p.age.toString } </td>
                                    </tr>
          }
        </table>
    }

    val people: AddressBook = new AddressBook(
      Person("Tom", 20),
      Person("Bob", 22),
      Person("James", 19))

    val e2: Elem =
      <html>
        <body>
          { people.toXHTML }
        </body>
      </html>
    val _e2: Elem = Serialize.read(Serialize.write(e2))
    Serialize.check(e2, _e2)
  }

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

  // Issue found with ISO-8859-1 in #121 that was fixed with UTF-8 default
  @UnitTest
  def writeReadNoDeclarationDefaultEncoding(): Unit = {
    val chars: Seq[Char] = ((32 to 126) ++ (160 to 255)).map(_.toChar)
    val xml: Elem = <x>{ chars.mkString }</x>

    // com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException:
    // Invalid byte 1 of 1-byte UTF-8 sequence.
    // scala.xml.XML.save("foo.xml", xml)
    // scala.xml.XML.loadFile("foo.xml").toString

    val outputStream: java.io.ByteArrayOutputStream = new java.io.ByteArrayOutputStream
    val streamWriter: java.io.OutputStreamWriter = new java.io.OutputStreamWriter(outputStream, "UTF-8")

    XML.write(streamWriter, xml, XML.encoding, xmlDecl = false, null)
    streamWriter.flush()

    val inputStream: java.io.ByteArrayInputStream = new java.io.ByteArrayInputStream(outputStream.toByteArray)
    val streamReader: java.io.InputStreamReader = new java.io.InputStreamReader(inputStream, XML.encoding)

    assertEquals(xml.toString, XML.load(streamReader).toString)
  }

  @UnitTest
  def t0663(): Unit = {
    val src: scala.io.Source = scala.io.Source.fromString("<?xml version='1.0' encoding='UTF-8'?><feed/>")
    val parser: ConstructingParser = ConstructingParser.fromSource(src, preserveWS = true)
    assertEquals("<feed/>", parser.document().toString)
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
  def t2771(): Unit = {
    val xml1: Elem = <root xmlns:ns="nsUri" ns:at="rootVal"><sub ns:at="subVal"/></root>
    val xml2: Elem = XML.loadString("""<root xmlns:ns="nsUri" ns:at="rootVal"><sub ns:at="subVal"/></root>""")

    def backslashSearch(x: Elem): String = "root:-" + (x \ "@{nsUri}at") + "-sub:-" + (x \ "sub" \ "@{nsUri}at") + "-"

    assertEquals("root:-rootVal-sub:-subVal-", backslashSearch(xml1))
    assertEquals("root:-rootVal-sub:-subVal-", backslashSearch(xml2))
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
  def t4387(): Unit = {
    import XML.loadString
    def mkElem(arg: String): Elem = <foo a="1" b="2" c="3" d="4" e={ arg }/>

    val x1: Elem = mkElem("5")
    val x2: Elem = mkElem("50")

    assertEquals(x1, loadString("" + x1))
    assertTrue(x2 != loadString("" + x1))
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
    def assertHonorsIterableContract(i: Iterable[_]): Unit = assertEquals(i.size.toLong, i.iterator.size.toLong)

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
  def t6939(): Unit = {
    val foo: Elem = <x:foo xmlns:x="http://foo.com/"><x:bar xmlns:x="http://bar.com/"><x:baz/></x:bar></x:foo>
    assertEquals(foo.child.head.scope.toString, """ xmlns:x="http://bar.com/"""")

    val fooDefault: Elem = <foo xmlns="http://foo.com/"><bar xmlns="http://bar.com/"><baz/></bar></foo>
    assertEquals(fooDefault.child.head.scope.toString, """ xmlns="http://bar.com/"""")

    val foo2: Elem = XML.loadString("""<x:foo xmlns:x="http://foo.com/"><x:bar xmlns:x="http://bar.com/"><x:baz/></x:bar></x:foo>""")
    assertEquals(foo2.child.head.scope.toString, """ xmlns:x="http://bar.com/"""")

    val foo2Default: Elem = XML.loadString("""<foo xmlns="http://foo.com/"><bar xmlns="http://bar.com/"><baz/></bar></foo>""")
    assertEquals(foo2Default.child.head.scope.toString, """ xmlns="http://bar.com/"""")
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
  def t9060(): Unit = {
    val expected: String = """<a xmlns:b·="http://example.com"/>"""
    assertEquals(expected, XML.loadString(expected).toString)
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
  def dontLoop(): Unit = {
    import java.io.{ Console => _, _ }

    val xml: String = "<!DOCTYPE xmeml SYSTEM 'uri'> <xmeml> <sequence> </sequence> </xmeml> "
    val sink: PrintStream = new PrintStream(new ByteArrayOutputStream())
    Console.withOut(sink) {
      Console.withErr(sink) {
        ConstructingParser.fromSource(io.Source.fromString(xml), preserveWS = true).document().docElem
      }
    }
  }

  /** Default SAXParserFactory */
  val defaultParserFactory: javax.xml.parsers.SAXParserFactory = javax.xml.parsers.SAXParserFactory.newInstance

  @throws(classOf[org.xml.sax.SAXNotRecognizedException])
  def issue17UnrecognizedFeature(): Unit = {
    assertTrue(defaultParserFactory.getFeature("foobar"))
  }

  @UnitTest
  def issue17SecureProcessing(): Unit = {
    assertTrue(defaultParserFactory.getFeature("http://javax.xml.XMLConstants/feature/secure-processing"))
  }

  @UnitTest
  def issue17ExternalGeneralEntities(): Unit = {
    assertTrue(defaultParserFactory.getFeature("http://xml.org/sax/features/external-general-entities"))
  }

  @UnitTest
  def issue17LoadExternalDtd(): Unit = {
    assertTrue(defaultParserFactory.getFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd"))
  }

  @UnitTest
  def issue17DisallowDoctypeDecl(): Unit = {
    assertFalse(defaultParserFactory.getFeature("http://apache.org/xml/features/disallow-doctype-decl"))
  }

  @UnitTest
  def issue17ExternalParameterEntities(): Unit = {
    assertTrue(defaultParserFactory.getFeature("http://xml.org/sax/features/external-parameter-entities"))
  }

  @UnitTest
  def issue17ResolveDtdUris(): Unit = {
    assertTrue(defaultParserFactory.getFeature("http://xml.org/sax/features/resolve-dtd-uris"))
  }

  @UnitTest
  def issue17isXIncludeAware(): Unit = {
    assertFalse(XML.parser.isXIncludeAware)
  }

  @UnitTest
  def issue17isNamespaceAware(): Unit = {
    assertFalse(XML.parser.isNamespaceAware)
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

  @UnitTest( expected = classOf[scala.xml.SAXParseException] )
  def issue35(): Unit = {
    val broken: String = "<broken attribute='is truncated"
    XML.loadString(broken)
  }

  def roundtrip(xml: String): Unit = assertEquals(xml, XML.loadString(xml).toString)

  @UnitTest
  def issue508commentParsing(): Unit = {
    // confirm that comments are processed correctly now
    roundtrip("<a><!-- comment --> suffix</a>")
    roundtrip("<a>prefix <!-- comment -->   <!-- comment2 --> suffix</a>")
    roundtrip("<a>prefix <b><!-- comment --></b> suffix</a>")
    roundtrip("<a>prefix <b><!-- multi-\nline\n comment --></b> suffix</a>")
    roundtrip("""<a>prefix <b><!-- multi-
                |line
                | comment --></b> suffix</a>""".stripMargin)

    // confirm that processing instructions were always processed correctly
    roundtrip("<a><?target content ?> suffix</a>")
    roundtrip("<a>prefix <?target content ?> suffix</a>")
    roundtrip("<a>prefix <b><?target content?> </b> suffix</a>")
  }

  @UnitTest
  def cdataParsing(): Unit = {
    roundtrip("<a><![CDATA[ cdata ]]> suffix</a>")
    roundtrip("<a>prefix <![CDATA[ cdata ]]> suffix</a>")
    roundtrip("<a>prefix <b><![CDATA[ cdata section]]></b> suffix</a>")
    roundtrip("""<a>prefix <b><![CDATA[
                | multi-
                | line    cdata
                |    section]]>   </b> suffix</a>""".stripMargin)
  }

  def roundtripNodes(xml: String): Unit = assertEquals(xml, XML.loadStringNodes(xml).map(_.toString).mkString(""))

  @UnitTest
  def xmlLoaderLoadNodes(): Unit = {
    roundtripNodes("<!-- prolog --><a>text</a>")
    roundtripNodes("<!-- prolog --><?target content ?><!-- comment2 --><a>text</a>")
    roundtripNodes("""<!-- prolog
                     |    --><?target content ?><!--
                     |  comment2 --><a>text</a>""".stripMargin)

    roundtripNodes("<a>text</a><!-- epilogue -->")
    roundtripNodes("<a>text</a><!-- epilogue --><?target content ?><!-- comment2 -->")

    // Note: at least with the JDK's Xerces, whitespace in the prolog and epilogue gets lost in parsing:
    // the parser does not fire any white-space related events, so:
    // does not work: roundtripNodes("<!-- c -->  <a/>")
    // does not work: roundtripNodes("<a/> <!-- epilogue -->")
  }

  // using non-namespace-aware parser, this always worked;
  // using namespace-aware parser, this works with FactoryAdapter enhanced to handle startPrefixMapping() events;
  // see https://github.com/scala/scala-xml/issues/506
  def roundtrip(namespaceAware: Boolean, xml: String): Unit = {
    val parserFactory: javax.xml.parsers.SAXParserFactory = javax.xml.parsers.SAXParserFactory.newInstance()
    parserFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true)
    parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    parserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    parserFactory.setFeature("http://xml.org/sax/features/resolve-dtd-uris", false)
    parserFactory.setNamespaceAware(namespaceAware)
    parserFactory.setXIncludeAware(namespaceAware)

    assertEquals(xml, XML.withSAXParser(parserFactory.newSAXParser).loadString(xml).toString)
  }

  @UnitTest
  def namespaceUnaware(): Unit =
    roundtrip(namespaceAware = false, """<book xmlns="http://docbook.org/ns/docbook" xmlns:xi="http://www.w3.org/2001/XInclude"/>""")

  @UnitTest
  def namespaceAware(): Unit =
    roundtrip(namespaceAware = true, """<book xmlns="http://docbook.org/ns/docbook" xmlns:xi="http://www.w3.org/2001/XInclude"/>""")

  @UnitTest
  def namespaceAware2(): Unit =
    roundtrip(namespaceAware = true, """<book xmlns="http://docbook.org/ns/docbook" xmlns:xi="http://www.w3.org/2001/XInclude"><svg xmlns:svg="http://www.w3.org/2000/svg"/></book>""")

  @UnitTest
  def useXMLReaderWithXMLFilter(): Unit = {
    val parent: org.xml.sax.XMLReader = javax.xml.parsers.SAXParserFactory.newInstance.newSAXParser.getXMLReader
    val filter: org.xml.sax.XMLFilter = new org.xml.sax.helpers.XMLFilterImpl(parent) {
      override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
        for (i <- 0 until length) if (ch(start+i) == 'a') ch(start+i) = 'b'
        super.characters(ch, start, length)
      }
    }
    assertEquals(XML.withXMLReader(filter).loadString("<a>caffeeaaay</a>").toString, "<a>cbffeebbby</a>")
  }

  @UnitTest
  def checkThatErrorHandlerIsNotOverwritten(): Unit = {
    var gotAnError: Boolean = false
    XML.reader.setErrorHandler(new org.xml.sax.ErrorHandler {
      override def warning(e: SAXParseException): Unit = gotAnError = true
      override def error(e: SAXParseException): Unit = gotAnError = true
      override def fatalError(e: SAXParseException): Unit = gotAnError = true
    })
    try {
      XML.loadString("<a>")
    } catch {
      case _: org.xml.sax.SAXParseException =>
    }
    assertTrue(gotAnError)
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
  def issue46(): Unit = {
    // val x = <node/>
    val x: Elem = <node></node>
    // val x = Elem(null, "node", e, sc)
    val pp: PrettyPrinter = new PrettyPrinter(80, 2)
    // This assertion passed
    assertEquals("<node></node>", x.toString)
    // This was the bug, producing <node></node>
    assertEquals("<node/>", pp.format(x.copy(minimizeEmpty = true)))
  }

  @UnitTest
  def issue90(): Unit = {
    val pp: PrettyPrinter = new PrettyPrinter(80, 2, minimizeEmpty = true)
    val x: Elem = <node><leaf></leaf></node>
    assertEquals("<node>\n  <leaf/>\n</node>", pp.format(x))
  }

  @UnitTest
  def issue231(): Unit = {
    val pp: PrettyPrinter = new PrettyPrinter(4, 2, minimizeEmpty = true)
    val x: Elem = <a b="c"/>
    val formatted: String = pp.format(x)
    val expected: String =
      """|<a
         |b="c"/>
         |""".stripMargin
    assertEquals(x, XML.loadString(formatted))
    assertEquals(expected, formatted)
  }

  @UnitTest
  def issue231_withoutAttributes(): Unit = {
    val pp: PrettyPrinter = new PrettyPrinter(4, 2, minimizeEmpty = true)
    val x: Elem = <abcdefg/>
    val expected: String =
      """|<abcdefg/>
         |""".stripMargin
    val formatted: String = pp.format(x)
    assertEquals(x, XML.loadString(formatted))
    assertEquals(expected, formatted)
  }

  @UnitTest
  def issue231_children(): Unit = {
    val pp: PrettyPrinter = new PrettyPrinter(4, 2, minimizeEmpty = true)
    val x: Elem = <a b="c"><d/><e><f g="h"></f><i/></e></a>
    val formatted: String = pp.format(x)
    val expected: String =
      """|<a 
         |b="c">
         |  <d
         |  />
         |  <e>
         |    <f
         |    g="h"/>
         |    <i
         |    />
         |  </e>
         |</a>
         |""".stripMargin
    assertEquals(expected, formatted)
  }

  @UnitTest
  def issue231_elementText(): Unit = {
    val pp: PrettyPrinter = new PrettyPrinter(4, 2, minimizeEmpty = true)
    val x: Elem = <a>x<b/><c>y</c><d/></a>
    val formatted: String = pp.format(x)
    val expected: String =
      """|<a>
         |  x
         |  <b
         |  />
         |  <c>
         |    y
         |  </c>
         |  <d
         |  />
         |</a>""".stripMargin
    assertEquals(expected, formatted)
  }

  def toSource(s: String): scala.io.Source = new scala.io.Source {
    override val iter: Iterator[Char] = s.iterator
    override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err): Unit = ()
  }

  @UnitTest
  def xTokenTest(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource("a"), preserveWS = false)
    assertEquals((): Unit, x.xToken('b'))
  }

  @UnitTest(expected = classOf[scala.xml.parsing.FatalError])
  def xCharDataFailure(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource(""), preserveWS = false)

    x.xCharData
  }

  @UnitTest(expected = classOf[scala.xml.parsing.FatalError])
  def xCommentFailure(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource(""), preserveWS = false)

    x.xComment
  }

  @UnitTest
  def xmlProcInstrTest(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource("aa"), preserveWS = false)

    assertEquals(new UnprefixedAttribute("aa", Text(""), Null), x.xmlProcInstr())
  }

  @UnitTest(expected = classOf[scala.xml.parsing.FatalError])
  def notationDeclFailure(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource(""), preserveWS = false)

    x.notationDecl()
  }

  @UnitTest
  def pubidLiteralTest(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource(""), preserveWS = false)

    assertEquals("", x.pubidLiteral())
  }

  @UnitTest
  def xAttributeValueTest(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource("'"), preserveWS = false)

    assertEquals("", x.xAttributeValue())
  }

  @UnitTest
  def xEntityValueTest(): Unit = {
    val x: ConstructingParser = ConstructingParser.fromSource(toSource(""), preserveWS = false)

    assertEquals("", x.xEntityValue())
  }
}
