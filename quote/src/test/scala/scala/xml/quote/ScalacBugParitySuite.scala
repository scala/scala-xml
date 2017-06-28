package scala.xml.quote

class ScalacBugParitySuite extends XmlQuoteSuite {

  test("empty CharRef") {
    assert(xml"""<a b="&#;"/>""" ≈ <a b="&#;"/>)
    assert(xml"""<a b="&#x;"/>""" ≈ <a b="&#x;"/>)
    assert(xml"""<a>&#;</a>""" ≈ <a>&#;</a>)
    assert(xml"""<a>&#x;</a>""" ≈ <a>&#x;</a>)
  }

  test("closing PCData tag in text") {
    assert(xml"""<a>]]></a> """ ≈ <a>]]></a> )
  }

  test("minimized empty group is not a group") {
    assert(xml"<xml:group/>" ≈ <xml:group/>)
    assert(xml"<xml:group/>" !≈ xml"<xml:group></xml:group>")
    assert(xml"<xml:group/>".isInstanceOf[xml.Elem])
  }

  test("malformed namespace") {
    assert(xml"""<a xmlnshello="scope1"/>""" ≈ <a xmlnshello="scope1"/>)
  }
}
