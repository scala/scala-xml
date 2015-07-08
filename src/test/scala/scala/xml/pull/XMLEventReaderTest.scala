package scala.xml.pull

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

import java.io.PrintStream
import scala.io.Source
import scala.xml.parsing.FatalError

class XMLEventReaderTest {

  def reading(text: String)(f: XMLEventReader => Unit): Unit = {
    val s = new Source {
      override protected val iter = Source fromString text
      override def report(pos: Int, msg: String, out: PrintStream) = ()
    }
    val r = new XMLEventReader(s)
    try f(r)
    finally r.stop()
  }

  @Test
  def pull() = reading("<hello><world/>!</hello>") { r =>
    assertTrue(r.next() match {
      case EvElemStart(_, "hello", _, _) => true
      case _ => false
    })
    assertTrue(r.next() match {
      case EvElemStart(_, "world", _, _) => true
      case _ => false
    })
    assertTrue(r.next() match {
      case EvElemEnd(_, "world") => true
      case _ => false
    })
    assertTrue(r.next() match {
      case EvText("!") => true
      case _ => false
    })
    assertTrue(r.next() match {
      case EvElemEnd(_, "hello") => true
      case _ => false
    })
  }

 @Test(expected = classOf[FatalError]) //expected closing tag of verbosegc
 def missingTagTest: Unit = {
   val data =
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

   reading(data) { r =>
     while (r.hasNext) r.next()
   }
 }
}
