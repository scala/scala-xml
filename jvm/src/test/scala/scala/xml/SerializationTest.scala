package scala.xml

import java.io._

import org.junit.Assert.assertEquals
import org.junit.Test

class SerializationTest {
  def roundTrip[T](obj: T): T = {
    def serialize(in: T): Array[Byte] = {
      val bos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(bos)
      oos.writeObject(in)
      oos.flush()
      bos.toByteArray()
    }

    def deserialize(in: Array[Byte]): T = {
      val bis = new ByteArrayInputStream(in)
      val ois = new ObjectInputStream(bis)
      ois.readObject.asInstanceOf[T]
    }

    deserialize(serialize(obj))
  }

  @Test
  def xmlLiteral: Unit = {
    val n = <node/>
    assertEquals(n, roundTrip(n))
  }

  @Test
  def empty: Unit = {
    assertEquals(NodeSeq.Empty, roundTrip(NodeSeq.Empty))
  }

  @Test
  def implicitConversion: Unit = {
    val parent = <parent><child></child><child/></parent>
    val children: Seq[Node] = parent.child
    val asNodeSeq: NodeSeq = children
    assertEquals(asNodeSeq, roundTrip(asNodeSeq))
  }
}
