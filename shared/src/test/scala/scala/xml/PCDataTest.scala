package scala.xml

import org.junit.Test
import org.junit.Assert.assertEquals

class PCDataTest {

  @Test
  def emptyTest = {
    val pcdata = new PCData("")
    assertEquals("<![CDATA[]]>", pcdata.toString)
  }

  @Test
  def bracketTest = {
    val pcdata = new PCData("[]")
    assertEquals("<![CDATA[[]]]>", pcdata.toString)
  }

  @Test
  def hellaBracketingTest = {
    val pcdata = new PCData("[[[[[[[[]]]]]]]]")
    assertEquals("<![CDATA[[[[[[[[[]]]]]]]]]]>", pcdata.toString)
  }

  @Test
  def simpleNestingTest = {
    val pcdata = new PCData("]]>")
    assertEquals("<![CDATA[]]]]><![CDATA[>]]>", pcdata.toString)
  }

  @Test
  def recursiveNestingTest = {
    val pcdata = new PCData("<![CDATA[]]>")
    assertEquals("<![CDATA[<![CDATA[]]]]><![CDATA[>]]>", pcdata.toString)
  }
}
