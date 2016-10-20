package scala.xml

import org.junit.Test
import org.junit.Assert.assertEquals

class NodeBufferTest {

  @Test
  def testToString: Unit = {
    val nodeBuffer = new NodeBuffer
    assertEquals("NodeBuffer()", nodeBuffer.toString)
  }
}
