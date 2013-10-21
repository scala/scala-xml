package scala.xml

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

class XMLEmbeddingTest {

  @Test
  def basic: Unit = {
    val ya = <x>{{</x>
    assertEquals("{", ya.text)
    val ua = <x>}}</x>
    assertEquals("}", ua.text)
    val za = <x>{{}}{{}}{{}}</x>
    assertEquals("{}{}{}", za.text)
  }

}