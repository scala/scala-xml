package scala.xml

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

class PatternMatching extends {
  @Test
  def unprefixedAttribute: Unit = {
    val li = List("1", "2", "3", "4")
    assertTrue(matchSeq(li))
    assertTrue(matchList(li))
  }

  def matchSeq(args: Seq[String]) = args match {
    case Seq(a, b, c, d @ _*) => true
  }

  def matchList(args: List[String]) =
    Elem(null, "bla", Null, TopScope, minimizeEmpty = true, (args map { x => Text(x) }): _*) match {
      case Elem(_, _, _, _, Text("1"), _*) => true
    }

  @Test
  def simpleNode =
    assertTrue(<hello/> match {
      case <hello/> => true
    })

  @Test
  def nameSpaced =
    assertTrue(<x:ga xmlns:x="z"/> match {
      case <x:ga/> => true
    })

  val cx = <z:hello foo="bar" xmlns:z="z" x:foo="baz" xmlns:x="the namespace from outer space">
             crazy text world
           </z:hello>

  @Test
  def nodeContents = {
    assertTrue(Utility.trim(cx) match {
      case n @ <hello>crazy text world</hello> if (n \ "@foo") xml_== "bar" => true
    })
    assertTrue(Utility.trim(cx) match {
      case n @ <z:hello>crazy text world</z:hello> if (n \ "@foo") xml_== "bar" => true
    })
    assertTrue(<x:foo xmlns:x="gaga"/> match {
      case scala.xml.QNode("gaga", "foo", md, child @ _*) => true
    })

    assertTrue(<x:foo xmlns:x="gaga"/> match {
      case scala.xml.Node("foo", md, child @ _*) => true
    })

  }

  object SafeNodeSeq {
    def unapplySeq(any: Any): Option[Seq[Node]] = any match {
      case s: Seq[_] => Some(s flatMap (_ match {
        case n: Node => n case _ => NodeSeq.Empty
      })) case _ => None
    }
  }

  @Test
  def nodeSeq = { // t0646
    import scala.xml.NodeSeq

    val books =
      <bks>
        <title>Blabla</title>
        <title>Blubabla</title>
        <title>Baaaaaaalabla</title>
      </bks>;

    assertTrue(new NodeSeq { val theSeq = books.child } match {
      case t @ Seq(<title>Blabla</title>) => false
      case _ => true
    })

    // SI-1059
    var m: PartialFunction[Any, Any] = { case SafeNodeSeq(s @ _*) => s }

    assertEquals(m(<a/> ++ <b/>), List(<a/>, <b/>))
    assertTrue(m.isDefinedAt(<a/> ++ <b/>))
  }

  @Test
  def SI_4124 = {
    val body: Node = <elem>hi</elem>

    assertTrue((body: AnyRef, "foo") match {
      case (node: Node, "bar") => false
      case (ser: Serializable, "foo") => true
    })

    assertTrue((body, "foo") match {
      case (node: Node, "bar") => false
      case (ser: Serializable, "foo") => true
    })

    assertTrue((body: AnyRef, "foo") match {
      case (node: Node, "foo") => true
      case (ser: Serializable, "foo") => false
    })

    assertTrue((body: AnyRef, "foo") match {
      case (node: Node, "foo") => true
      case (ser: Serializable, "foo") => false
    })
  }
}