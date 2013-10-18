package scala.xml.pull

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

}