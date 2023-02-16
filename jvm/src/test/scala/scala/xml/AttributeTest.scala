package scala.xml

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

class AttributeTestJVM {

  @Test
  def attributeOrder(): Unit = {
    val x: Elem = <x y="1" z="2"/>
    assertEquals("""<x y="1" z="2"/>""", x.toString)
  }

  @Test
  def attributesFromString(): Unit = {
    val str: String = """<x y="1" z="2"/>"""
    val doc: Elem = XML.loadString(str)
    assertEquals(str, doc.toString)
  }

  @Test
  def attributesAndNamespaceFromString(): Unit = {
    val str: String = """<x xmlns:w="w" y="1" z="2"/>"""
    val doc: Elem = XML.loadString(str)
    assertNotEquals(str, doc.toString)
    val str2: String = """<x y="1" z="2" xmlns:w="w"/>"""
    val doc2: Elem = XML.loadString(str2)
    assertEquals(str2, doc2.toString)
  }

  @Test(expected=classOf[SAXParseException])
  def attributesFromStringWithDuplicate(): Unit = {
    val str: String = """<elem one="test" one="test1" two="test2" three="test3"></elem>"""
    XML.loadString(str)
  }
}
