package scala.xml

import scala.collection.Seq
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

class PatternMatchingTest {
  @Test
  def unprefixedAttribute(): Unit = {
    val li: List[String] = List("1", "2", "3", "4")
    assertTrue(matchSeq(li))
    assertTrue(matchList(li))
  }

  def matchSeq(args: Seq[String]): Boolean = args match {
    case Seq(a, b, c, d @ _*) => true
  }

  def matchList(args: List[String]): Boolean =
    Elem(null, "bla", Null, TopScope, minimizeEmpty = true, args.map { x => Text(x) }: _*) match {
      case Elem(_, _, _, _, Text("1"), _*) => true
    }

  @Test
  def simpleNode(): Unit =
    assertTrue(<hello/> match {
      case <hello/> => true
    })

  @Test
  def nameSpaced(): Unit =
    assertTrue(<x:ga xmlns:x="z"/> match {
      case <x:ga/> => true
    })

  val cx: Elem = <z:hello foo="bar" xmlns:z="z" x:foo="baz" xmlns:x="the namespace from outer space">
                   crazy text world
                 </z:hello>

  @Test
  def nodeContents(): Unit = {
    assertTrue(Utility.trim(cx) match {
      case n @ <hello>crazy text world</hello> if (n \ "@foo") xml_== "bar" => true
    })
    assertTrue(Utility.trim(cx) match {
      case n @ <z:hello>crazy text world</z:hello> if (n \ "@foo") xml_== "bar" => true
    })
    assertTrue(<x:foo xmlns:x="gaga"/> match {
      case QNode("gaga", "foo", md, child @ _*) => true
    })

    assertTrue(<x:foo xmlns:x="gaga"/> match {
      case Node("foo", md, child @ _*) => true
    })
  }

  object SafeNodeSeq {
    def unapplySeq(any: Any): Option[Seq[Node]] = any match {
      case s: Seq[_] => Some(s flatMap {
        case n: Node => n
        case _ => NodeSeq.Empty
      })
      case _ => None
    }
  }

  @Test
  def nodeSeq(): Unit = { // t0646
    val books: Elem =
      <bks>
        <title>Blabla</title>
        <title>Blubabla</title>
        <title>Baaaaaaalabla</title>
      </bks>

    assertTrue(NodeSeq.fromSeq(books.child) match {
      case t @ Seq(<title>Blabla</title>) => false
      case _ => true
    })

    // SI-1059
    val m: PartialFunction[Any, Any] = { case SafeNodeSeq(s @ _*) => s }

    assertEquals(m(<a/> ++ <b/>), List(<a/>, <b/>))
    assertTrue(m.isDefinedAt(<a/> ++ <b/>))
  }

  @Test
  def SI_4124(): Unit = {
    val body: Node = <elem>hi</elem>

    assertTrue((body: AnyRef, "foo") match {
      case (node: Node, "bar") => false
      case (ser: Serializable, "foo") => true
      case (_, _) => false
    })

    assertTrue((body, "foo") match {
      case (node: Node, "bar") => false
      case (ser: Serializable, "foo") => true
      case (_, _) => false
    })

    assertTrue((body: AnyRef, "foo") match {
      case (node: Node, "foo") => true
      case (ser: Serializable, "foo") => false
      case (_, _) => false
    })

    assertTrue((body: AnyRef, "foo") match {
      case (node: Node, "foo") => true
      case (ser: Serializable, "foo") => false
      case (_, _) => false
    })
  }
}
