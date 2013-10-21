package scala.xml

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

class XMLTest {

  @Test
  def nodeSeq: Unit = {
    val p = <foo>
    <bar gt='ga' value="3"/>
    <baz bazValue="8"/>
    <bar value="5" gi='go'/>
    </foo>

    val pelems_1 = for (x <- p \ "bar"; y <- p \ "baz" ) yield {
      Text(x.attributes("value").toString + y.attributes("bazValue").toString+ "!")
    };

    val pelems_2 = new NodeSeq { val theSeq = List(Text("38!"),Text("58!")) };
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
    <entry><title>Blabla</title>
    <remarks>
    Hallo Welt.
    </remarks>
    </entry>
    <entry><title>Blubabla</title>
    <remarks>
    Hello Blu
    </remarks>
    </entry>
    <entry><title>Blubabla</title>
    <remarks>
    rem 2
    </remarks>
    </entry>
    </reviews>;

    val results1 = new scala.xml.PrettyPrinter(80, 5).formatNodes (
      for (t <- books \\ "title";
           r <- reviews \\ "entry"
           if (r \ "title") xml_== t) yield
             <result>
      { t }
      { r \ "remarks" }
      </result>
    );
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
    This is the <b>phonebook</b> of the
    <a href="http://acme.org">ACME</a> corporation.
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
    This is the <b>addressbook</b> of the
    <a href="http://acme.org">ACME</a> corporation.
    </descr>
    <entry>
    <name>John</name>
    <street> Elm Street</street>
    <city>Dolphin City</city>
    </entry>
    </addrbook>;

    val actual: String = new scala.xml.PrettyPrinter(80, 5).formatNodes (
      for (t <- addrBook \\ "entry";
           r <- phoneBook \\ "entry"
           if (t \ "name") xml_== (r \ "name")) yield
             <result>
      { t.child }
      { r \ "phone" }
      </result>
    )
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
    for (n <- cuckoo \ "_" ) {
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
      Star(Letter(ElemName("baz"))) )));
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

    assertTrue(vtor(<foo><bar/><baz/><baz/></foo> ))
    assertTrue(vtor(<foo>ab<bar/>cd<baz/>ed<baz/>gh</foo> ))
    assertFalse(vtor(<foo> <ugha/> <bugha/> </foo> ))
  }

  def validationfOfAttributes: Unit = {
    val vtor = new scala.xml.dtd.ElementValidator();
    vtor.setContentModel(null)
    vtor.setMetaData(List())
    assertFalse(vtor( <foo bar="hello"/> ))

    {
      import scala.xml.dtd._
      vtor setMetaData List(AttrDecl("bar", "CDATA", IMPLIED))
    }
    assertFalse(vtor(<foo href="http://foo.com" bar="hello"/>))
    assertTrue(vtor(<foo bar="hello"/>))

    {
      import scala.xml.dtd._
      vtor.setMetaData(List(AttrDecl("bar","CDATA",REQUIRED)))
    }
    assertFalse(vtor( <foo href="http://foo.com" /> ))
    assertTrue( vtor( <foo bar="http://foo.com" /> ))
  }

}
