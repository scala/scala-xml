package scala.xml

import scala.collection.Seq
import org.junit.Assert.assertEquals
import org.junit.Test

class SerializationTest {
  @Test
  def xmlLiteral(): Unit = {
    val n: Elem = <node/>
    assertEquals(n, JavaByteSerialization.roundTrip(n))
  }

  @Test
  def empty(): Unit = {
    assertEquals(NodeSeq.Empty, JavaByteSerialization.roundTrip(NodeSeq.Empty))
  }

  @Test
  def unmatched(): Unit = {
    assertEquals(NodeSeq.Empty, JavaByteSerialization.roundTrip(<xml/> \ "HTML"))
  }

  @Test
  def implicitConversion(): Unit = {
    val parent: Elem = <parent><child></child><child/></parent>
    val children: Seq[Node] = parent.child
    val asNodeSeq: NodeSeq = children // implicit seqToNodeSeq
    assertEquals(asNodeSeq, JavaByteSerialization.roundTrip(asNodeSeq))
  }
}
