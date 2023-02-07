package scala.xml

import org.junit.Test
import org.junit.Assert.assertEquals

class PCDataTest {

  def check(pcdata: String, expected: String): Unit = {
    val actual: PCData = new PCData(pcdata)
    assertEquals(expected, actual.toString)
  }

  @Test
  def emptyTest(): Unit = check("", "<![CDATA[]]>")

  @Test
  def bracketTest(): Unit = check("[]", "<![CDATA[[]]]>")

  @Test
  def hellaBracketingTest(): Unit = check("[[[[[[[[]]]]]]]]", "<![CDATA[[[[[[[[[]]]]]]]]]]>")

  @Test
  def simpleNestingTest(): Unit = check("]]>", "<![CDATA[]]]]><![CDATA[>]]>")

  @Test
  def recursiveNestingTest(): Unit = check("<![CDATA[]]>", "<![CDATA[<![CDATA[]]]]><![CDATA[>]]>")
}
