package scala.xml

import org.junit.Test
import org.junit.Assert.assertEquals

class XMLEmbeddingTest {

  @Test
  def basic(): Unit = {
    val ya: Elem = <x>{{</x>
    assertEquals("{", ya.text)
    val ua: Elem = <x>}}</x>
    assertEquals("}", ua.text)
    val za: Elem = <x>{{}}{{}}{{}}</x>
    assertEquals("{}{}{}", za.text)
  }
}
