package scala.xml

import scala.xml.NodeSeq.seqToNodeSeq

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.fail

class NodeSeqTest {

  @Test
  def testAppend: Unit = { // Bug #392.
    val a: NodeSeq = <a>Hello</a>
    val b = <b>Hi</b>
    a ++ <b>Hi</b> match {
      case res: NodeSeq => assertEquals(2, res.size)
      case res: Seq[Node] => fail("Should be NodeSeq") // Unreachable code?
    }
    val res: NodeSeq = a ++ b
    val exp = NodeSeq.fromSeq(Seq(<a>Hello</a>, <b>Hi</b>))
    assertEquals(exp, res)
  }
}
