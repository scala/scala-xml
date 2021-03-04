package scala.xml

import language.postfixOps

import org.junit.{Test => UnitTest}
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals

class XMLTest2x {
  // t-486
  def wsdlTemplate3(serviceName: String): Node =
    <wsdl:definitions name={ serviceName } xmlns:tns={ new _root_.scala.xml.Text("target3") }>
    </wsdl:definitions>;

  @UnitTest
  def wsdl = {
    assertEquals("""<wsdl:definitions name="service3" xmlns:tns="target3">
    </wsdl:definitions>""", wsdlTemplate3("service3") toString)
  }

  @UnitTest
  def t5154: Unit = {

    // extra space made the pattern OK
    def f = <z> {{3}}</z> match { case <z> {{3}}</z> => true }

    // lack of space used to error: illegal start of simple pattern
    def g = <z>{{3}}</z> match { case <z>{{3}}</z> => true }

    assertTrue(f)
    assertTrue(g)
  }

}
