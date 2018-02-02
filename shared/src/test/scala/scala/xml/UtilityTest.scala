package scala.xml

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

class UtilityTest {

  @Test
  def isNameStart: Unit = {
    assertTrue(Utility.isNameStart('b'))
    assertTrue(Utility.isNameStart(':'))
  }

  @Test
  def trim: Unit = {
    val x = <foo>
                 <toomuchws/>
              </foo>
    val y = xml.Utility.trim(x)
    assertTrue(y match { case <foo><toomuchws/></foo> => true })

    val x2 = <foo>
      <toomuchws>  a b  b a  </toomuchws>
    </foo>
    val y2 = xml.Utility.trim(x2)
    assertTrue(y2 match { case <foo><toomuchws>a b b a</toomuchws></foo> => true })
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

  @Test
  def issue90: Unit = {
    val x = <node><leaf></leaf></node>
    assertEquals("<node><leaf/></node>", Utility.serialize(x, minimizeTags = MinimizeMode.Always).toString)
  }

  @Test
  def issue183: Unit = {
    val x = <node><!-- comment  --></node>
    assertEquals("<node></node>", Utility.serialize(x, stripComments = true).toString)
    assertEquals("<node><!-- comment  --></node>", Utility.serialize(x, stripComments = false).toString)
  }

}
