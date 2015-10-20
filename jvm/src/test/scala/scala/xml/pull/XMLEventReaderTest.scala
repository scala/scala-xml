package scala.xml
package pull

import org.junit.Test
import org.junit.Assert.{assertFalse, assertTrue}

import scala.io.Source
import scala.xml.parsing.FatalError

class XMLEventReaderTest {

  val src = Source.fromString("<hello><world/>!</hello>")

  private def toSource(s: String) = new Source {
    val iter = s.iterator
    override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err) {}
  }

  @Test
  def pull: Unit = {
    val er = new XMLEventReader(src)
    assertTrue(er.next match {
      case EvElemStart(_, "hello", _, _) => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvElemStart(_, "world", _, _) => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvElemEnd(_, "world") => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvText("!") => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvElemEnd(_, "hello") => true
      case _ => false
    })
    er.stop  // allow thread to be garbage-collected
  }

  @Test
  def issue35: Unit = {
    val broken = "<broken attribute='is truncated"
    val r = new XMLEventReader(toSource(broken))

    assertTrue(r.next.isInstanceOf[EvElemStart])
  }

  @Test(expected = classOf[FatalError])
  def malformedCDATA: Unit = {
    val data = "<broken><![CDATA[A"
    val r = new XMLEventReader(toSource(data))

    assertTrue(r.next.isInstanceOf[EvElemStart])
    // error when returning EvText of CDATA
    r.next
  }

  @Test(expected = classOf[FatalError])
  def malformedComment1: Unit = {
    val data = "<!"
    val r = new XMLEventReader(toSource(data))

    // error when returning EvComment
    r.next
  }

  @Test(expected = classOf[FatalError])
  def malformedComment2: Unit = {
    val data = "<!-- comment "
    val r = new XMLEventReader(toSource(data))

    // error when returning EvComment
    r.next
  }

  @Test
  def malformedDTD1: Unit = {
    // broken ELEMENT
    val data =
      """<?xml version="1.0" encoding="utf-8"?>
        |<!DOCTYPE broken [
        |  <!ELE
      """.stripMargin
    val r = new XMLEventReader(toSource(data))

    assertFalse(r.hasNext)
  }

  @Test
  def malformedDTD2: Unit = {
    val data =
      """<!DOCTYPE broken [
        |  <!ELEMENT data (#PCDATA)>
      """.stripMargin
    val r = new XMLEventReader(toSource(data))

    assertFalse(r.hasNext)
  }

  @Test
  def malformedDTD3: Unit = {
    // broken ATTLIST
    val data =
      """<!DOCTYPE broken [
        |  <!ATTL
      """.stripMargin
    val r = new XMLEventReader(toSource(data))

    assertFalse(r.hasNext)
  }

  @Test
  def malformedDTD4: Unit = {
    // unexpected declaration
    val data =
      """<!DOCTYPE broken [
        |  <!UNEXPECTED
      """.stripMargin
    val r = new XMLEventReader(toSource(data))

    assertFalse(r.hasNext)
  }

  @Test
  def malformedDTD5: Unit = {
    val data =
      """<!DOCTYPE broken [
        |  <!ENTITY % foo 'INCLUDE'>
        |  <![%foo;[
      """.stripMargin
    val r = new XMLEventReader(toSource(data))

    assertFalse(r.hasNext)
  }

  @Test
  def malformedDTD6: Unit = {
    val data =
      """<!DOCTYPE broken [
        |  <!ENTITY % foo 'IGNORE'>
        |  <![%foo;[
      """.stripMargin
    val r = new XMLEventReader(toSource(data))

    assertFalse(r.hasNext)
  }

  @Test(expected = classOf[Exception])
  def missingTagTest: Unit = {
    val data=
      """<?xml version="1.0" ?>
        |<verbosegc xmlns="http://www.ibm.com/j9/verbosegc">
        |
        |<initialized id="1" timestamp="2013-10-04T00:11:08.389">
        |</initialized>
        | 
        |<exclusive-start id="2" timestamp="2013-10-04T00:11:09.185" intervalms="796.317">
        |<response-info timems="0.007" idlems="0.007" threads="0" />
        |</exclusive-start>
        |""".stripMargin

    val er = new XMLEventReader(toSource(data))
    while(er.hasNext) er.next()
    er.stop()
  }

  @Test
  def entityRefTest: Unit = {
    val source = Source.fromString("<text>&quot;&apos;&lt;&gt;&amp;</text>")
    val er = new XMLEventReader(source)

    assertTrue(er.next match {
      case EvElemStart(_, "text", _, _) => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvEntityRef("quot") => true
      case e => false
    })
    assertTrue(er.next match {
      case EvEntityRef("apos") => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvEntityRef("lt") => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvEntityRef("gt") => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvEntityRef("amp") => true
      case _ => false
    })
    assertTrue(er.next match {
      case EvElemEnd(_, "text") => true
      case _ => false
    })
    assert(er.isEmpty)
  }
}
