package scala.xml
package pull

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

import scala.io.Source

class XMLEventReaderTest {

  val src = Source.fromString("<hello><world/>!</hello>")

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
    val x = new Source {
      val iter = broken.iterator
      override def reportError(pos: Int, msg: String, out: java.io.PrintStream = Console.err) {}
    }
    val r = new XMLEventReader(x)
    assertTrue(r.next.isInstanceOf[EvElemStart])
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

   val er = new XMLEventReader(Source.fromString(data))
   while(er.hasNext) er.next()
   er.stop()
 }
}
