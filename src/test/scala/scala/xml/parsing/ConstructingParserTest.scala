package scala.xml
package parsing

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert
import Assert._
import scala.xml.JUnitAssertsForXML.assertEquals

class ConstructingParserTest {

  import scala.io.Source.fromString
  import ConstructingParser.fromSource
  //import scala.xml.{ NodeSeq, TopScope }
  import XML.loadString

  private def parse(s:String) = fromSource(fromString(s), false).element(TopScope)

  // fifth edition
  // ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] |
  // [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] |
  // [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
  // and
  // "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
  //
  @Test def `SI-9060: valid chars in names`(): Unit = {
    // promote chars to ints
    def c(a: Int) = a to a
    def cs(a: Int, b: Int) = a to b
    val ranges = List(
      c(':'), cs('A','Z'), cs('a','z'), cs('_','_'),
      0xC0 to 0x2FF filter { case 0xD7 | 0xF7 => false case _ => true },
      0x370 to 0x37D, 0x37F to 0x1FFF, 0x200C to 0x200D, 0x2070 to 0x218F,
      0x2C00 to 0x2FEF, 0x3001 to 0xD7FF, 0xF900 to 0xFDCF, 0xFDF0 to 0xFFFD
      // , 0x10000 to 0xEFFFF  // TODO, code points
    )
    val others = List[Range](
      c('-'), c('.'), '0'.toInt to '9', 0xB7 to 0xB7, 0x300 to 0x36F, 0x203F to 0x2040
    )

    val tester = new TokenTests { }
    for (r <- ranges ; i <- r) assertTrue(f"NameStart: $i%#x (${i.toChar})", tester.isNameStart(i.toChar))
    for (r <- others ; i <- r) assertTrue(f"NameChar: $i%#x (${i.toChar})", tester.isNameChar(i.toChar))
  }

  // notice the middle dot
  @Test def `SI-9060: problematic char in name`(): Unit = {
    val problematic = """<a xmlns:b·="http://example.com"/>"""
    val expected = problematic
    assertEquals(expected, parse(problematic))
    assertEquals(expected, loadString(problematic))
  }
}
