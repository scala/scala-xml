package scala.xml

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.fail

import scala.collection.immutable

class NodeSeqTest {

  @Test
  def testAppend(): Unit = { // Bug #392.
    val a: NodeSeq = <a>Hello</a>
    val b: Elem = <b>Hi</b>
    a ++ <b>Hi</b> match {
      case res: NodeSeq => assertEquals(2, res.size.toLong)
      case _: Seq[Node] => fail("Should be NodeSeq was Seq[Node]") // Unreachable code?
    }
    val res: NodeSeq = a ++ b
    val exp: NodeSeq = NodeSeq.fromSeq(Seq(<a>Hello</a>, <b>Hi</b>))
    assertEquals(exp, res)
  }

  @Test
  def testAppendedAll(): Unit = { // Bug #392.
    val a: NodeSeq = <a>Hello</a>
    val b: Elem = <b>Hi</b>
    a :+ <b>Hi</b> match {
      case res: Seq[Node] => assertEquals(2, res.size.toLong)
      case _: NodeSeq => fail("Should be Seq[Node] was NodeSeq") // Unreachable code?
    }
    val res: NodeSeq = a :+ b // implicit seqToNodeSeq
    val exp: NodeSeq = NodeSeq.fromSeq(Seq(<a>Hello</a>, <b>Hi</b>))
    assertEquals(exp, res)
  }

  @Test
  def testPrepended(): Unit = {
    val a: NodeSeq = <a>Hello</a>
    val b: Elem = <b>Hi</b>
    a +: <b>Hi</b> match {
      case res: Seq[Node] => assertEquals(2, res.size.toLong)
      case _: NodeSeq => fail("Should be Seq[Node] was NodeSeq") // Unreachable code?
    }
    val res: Seq[NodeSeq] = a +: b
    val exp: NodeBuffer = {
      <a>Hello</a><b>Hi</b>
    }
    assertEquals(exp.toSeq, res)
  }

  @Test
  def testPrependedAll(): Unit = {
    val a: NodeSeq = <a>Hello</a>
    val b: Elem = <b>Hi</b>
    val c: Elem = <c>Hey</c>
    a ++: <b>Hi</b> ++: <c>Hey</c> match {
      case res: Seq[Node] => assertEquals(3, res.size.toLong)
      case _: NodeSeq => fail("Should be Seq[Node] was NodeSeq") // Unreachable code?
    }
    val res: NodeSeq = a ++: b ++: c // implicit seqToNodeSeq
    val exp: NodeSeq = NodeSeq.fromSeq(Seq(<a>Hello</a>, <b>Hi</b>, <c>Hey</c>))
    assertEquals(exp, res)
  }

  @Test
  def testMap(): Unit = {
    val a: NodeSeq = <a>Hello</a>
    val exp: NodeSeq = Seq(<b>Hi</b>) // implicit seqToNodeSeq
    assertEquals(exp, a.map(_ => <b>Hi</b>))
    assertEquals(exp, for { _ <- a } yield { <b>Hi</b> })
  }

  @Test
  def testFlatMap(): Unit = {
    val a: NodeSeq = <a>Hello</a>
    val exp: NodeSeq = Seq(<b>Hi</b>) // implicit seqToNodeSeq
    assertEquals(exp, a.flatMap(_ => Seq(<b>Hi</b>)))
    assertEquals(exp, for { b <- a; _ <- b } yield { <b>Hi</b> })
    assertEquals(exp, for { b <- a; c <- b; _ <- c } yield { <b>Hi</b> })
  }

  @Test
  def testStringProjection(): Unit = {
    val a: Elem =
      <a>
        <b>b</b>
        <b>
          <c d="d">
            <e>e</e>
            <e>e</e>
          </c>
          <c>c</c>
        </b>
      </a>
    val res: Seq[String] = for {
      b <- a \ "b"
      c <- b.child
      e <- (c \ "e").headOption
    } yield {
      e.text.trim
    }
    assertEquals(Seq("e"), res)
  }
}
