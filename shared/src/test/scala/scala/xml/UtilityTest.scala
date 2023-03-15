package scala.xml

import scala.collection.Seq
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

class UtilityTest {

  @Test
  def isNameStart(): Unit = {
    assertTrue(Utility.isNameStart('b'))
    assertTrue(Utility.isNameStart(':'))
  }

  @Test
  def trim(): Unit = {
    val x: Elem = <foo>
                     <toomuchws/>
                  </foo>
    val y: Node = Utility.trim(x)
    assertTrue(y match { case <foo><toomuchws/></foo> => true })

    val x2: Elem = <foo>
      <toomuchws>  a b  b a  </toomuchws>
    </foo>
    val y2: Node = Utility.trim(x2)
    assertTrue(y2 match { case <foo><toomuchws>a b b a</toomuchws></foo> => true })
  }

  @Test
  def aposEscaping(): Unit = {
    val z: Elem = <bar>''</bar>
    val z1: String = z.toString
    assertEquals("<bar>''</bar>", z1)
  }

  @Test
  def sort(): Unit = {
    assertEquals("", Utility.sort(<a/>.attributes).toString)
    assertEquals(""" b="c"""", Utility.sort(<a b="c"/>.attributes).toString)
    val q: Node = Utility.sort(<a g='3' j='2' oo='2' a='2'/>)
    assertEquals(" a=\"2\" g=\"3\" j=\"2\" oo=\"2\"", Utility.sort(q.attributes).toString)
    val pp: PrettyPrinter = new PrettyPrinter(80,5)
    assertEquals("<a a=\"2\" g=\"3\" j=\"2\" oo=\"2\"/>", pp.format(q))
  }

  @Test
  def issue777(): Unit = {
    <hi>
      <there/>
      <guys/>
    </hi>.hashCode // Bug #777
  }

  @Test
  def issue90(): Unit = {
    val x: Elem = <node><leaf></leaf></node>
    assertEquals("<node><leaf/></node>", Utility.serialize(x, minimizeTags = MinimizeMode.Always).toString)
  }

  @Test
  def issue183(): Unit = {
    val x: Elem = <node><!-- comment  --></node>
    assertEquals("<node></node>", Utility.serialize(x, stripComments = true).toString)
    assertEquals("<node><!-- comment  --></node>", Utility.serialize(x, stripComments = false).toString)
  }

  val printableAscii: Seq[Char] = {
    (' ' to '/') ++ // Punctuation
    ('0' to '9') ++ // Digits
    (':' to '@') ++ // Punctuation (cont.)
    ('A' to 'Z') ++ // Uppercase
    ('[' to '`') ++ // Punctuation (cont.)
    ('a' to 'z') ++ // Lowercase
    ('{' to '~')    // Punctuation (cont.)
  }

  val escapedChars: Seq[Char] =
    Utility.Escapes.escMap.keys.toSeq

  @Test
  def escapePrintablesTest(): Unit = {
    for {
      char <- printableAscii.diff(escapedChars)
    } yield {
      assertEquals(char.toString, Utility.escape(char.toString))
    }
  }

  @Test
  def escapeEscapablesTest(): Unit = {
    for {
      char <- escapedChars
    } yield {
      assertNotEquals(char.toString, Utility.escape(char.toString))
    }
  }

  @Test
  def escapeAsciiControlCharsTest(): Unit = {

    /* Escapes that Scala (Java) doesn't support.
     * \u0007 -> \a  (bell)
     * \u001B -> \e  (escape)
     * \u000B -> \v  (vertical tab)
     * \u007F -> DEL (delete)
     */
    val input: String = " \u0007\b\u001B\f\n\r\t\u000B\u007F"

    val expect: String = " \n\r\t\u007F"

    val result: String = Utility.escape(input)

    assertEquals(printfc(expect), printfc(result)) // Pretty,
    assertEquals(expect, result)                   // but verify.
  }

  @Test
  def escapeUnicodeExtendedControlCodesTest(): Unit = {
    for {
      char <- '\u0080' to '\u009f' // Extended control codes (C1)
    } yield {
      assertEquals(char.toString, Utility.escape(char.toString))
    }
  }

  @Test
  def escapeUnicodeTwoBytesTest(): Unit = {
    for {
      char <- '\u00A0' to '\u07FF' // Two bytes (cont.)
    } yield {
      assertEquals(char.toString, Utility.escape(char.toString))
    }
  }

  @Test
  def escapeUnicodeThreeBytesTest(): Unit = {
    for {
      char <- '\u0800' to '\uFFFF' // Three bytes
    } yield {
      assertEquals(char.toString, Utility.escape(char.toString))
    }
  }

  /**
   * Human-readable character printing
   * 
   * Think of `od -c` of unix od(1) command.
   * 
   * Or think of `printf("%c", i)` in C, but a little better.
   */
  def printfc(str: String): String = {
    str.map(prettyChar).mkString
  }

  /**
   * Visual representation of characters that enhances output of
   * failed test assertions.
   */
  val prettyChar: Map[Char,String] = Map(
    '\u0000' -> "\\0", // Null
    '\u0001' -> "^A",  // Start of header
    '\u0002' -> "^B",  // Start of text
    '\u0003' -> "^C",  // End of text
    '\u0004' -> "^D",  // End of transmission
    '\u0005' -> "^E",  // Enquiry
    '\u0006' -> "^F",  // Acknowledgment
    '\u0007' -> "\\a", // Bell (^G)
    '\b'     -> "\\b", // Backspace (^H)
    '\t'     -> "\\t", // Tab (^I)
    '\n'     -> "\\n", // Newline (^J)
    '\u000B' -> "\\v", // Vertical tab (^K)
    '\f'     -> "\\f", // Form feed (^L)
    '\r'     -> "\\r", // Carriage return (^M)
    '\u000E' -> "^N",  // Shift out
    '\u000F' -> "^O",  // Shift in
    '\u0010' -> "^P",  // Data link escape
    '\u0011' -> "^Q",  // DC1 (XON)
    '\u0012' -> "^R",  // DC2
    '\u0013' -> "^S",  // DC3 (XOFF)
    '\u0014' -> "^T",  // DC4
    '\u0015' -> "^U",  // Negative acknowledgment
    '\u0016' -> "^V",  // Synchronous idle
    '\u0017' -> "^W",  // End of transmission block
    '\u0018' -> "^X",  // Cancel
    '\u0019' -> "^Y",  // End of medium
    '\u001A' -> "^Z",  // Substitute
    '\u001B' -> "\\e", // Escape
    '\u001C' -> "^\\", // File separator
    '\u001D' -> "^]",  // Group separator
    '\u001E' -> "^^",  // Record separator
    '\u001F' -> "^_",  // Unit separator
    '\u007F' -> "^?"   // Delete
  ).withDefault {
    (key: Char) => key.toString
  }

  def issue73StartsWithAndEndsWithWSInFirst(): Unit = {
    val x: Elem = <div>{Text("    My name is ")}{Text("Harry")}</div>
    assertEquals(<div>My name is Harry</div>, Utility.trim(x))
  }

  @Test
  def issue73EndsWithWSInLast(): Unit = {
    val x: Elem = <div>{Text("My name is ")}{Text("Harry    ")}</div>
    assertEquals(<div>My name is Harry</div>, Utility.trim(x)) 
  }

  @Test
  def issue73HasWSInMiddle(): Unit = {
    val x: Elem = <div>{Text("My name is")}{Text(" ")}{Text("Harry")}</div>
    assertEquals(<div>My name is Harry</div>, Utility.trim(x))
  }

  @Test
  def issue73HasWSEverywhere(): Unit = {
    val x: Elem = <div>{Text("   My name ")}{Text("  is  ")}{Text("  Harry   ")}</div>
    assertEquals(<div>My name is Harry</div>, Utility.trim(x))
  }

  @Test
  def issue306InvalidUnclosedEntity(): Unit = {
    val entity = "&# test </body></html>"
    val it: Iterator[Char] = entity.iterator
    var c = it.next()
    val next = () => if (it.hasNext) c = it.next() else c = 0.asInstanceOf[Char]
    val result = Utility.parseCharRef({ () => c }, next, _ => {}, _ => {})
    assertEquals("", result)
  }

}
