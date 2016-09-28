package scala.xml

import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals

class UtilityTest {

  @Test
  def isNameStart: Unit = {
    assertTrue(Utility.isNameStart('b'))
    assertFalse(Utility.isNameStart(':'))
  }

  @Test
  def trim: Unit = {
    val x = <foo>
                 <toomuchws/>
              </foo>
    val y = xml.Utility.trim(x)
    assertEquals(1, y match { case <foo><toomuchws/></foo> => 1 })

    val x2 = <foo>
      <toomuchws>  a b  b a  </toomuchws>
    </foo>
    val y2 = xml.Utility.trim(x2)
    assertEquals(2, y2 match { case <foo><toomuchws>a b b a</toomuchws></foo> => 2 })
  }

  @Test
  def aposEscaping: Unit = {
    val z = <bar>''</bar>
    val z1 = z.toString
    assertEquals("<bar>''</bar>", z1)
  }

  @Test
  def sort: Unit = {
    val q = xml.Utility.sort(<a g='3' j='2' oo='2' a='2'/>)
    assertEquals(" a=\"2\" g=\"3\" j=\"2\" oo=\"2\"", xml.Utility.sort(q.attributes).toString)
    val pp = new xml.PrettyPrinter(80,5)
    assertEquals("<a a=\"2\" g=\"3\" j=\"2\" oo=\"2\"/>", pp.format(q))
  }

  @Test
  def issue777: Unit = {
    <hi>
      <there/>
      <guys/>
    </hi>.hashCode // Bug #777
  }

}
