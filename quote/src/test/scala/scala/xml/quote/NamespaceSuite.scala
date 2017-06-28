package scala.xml.quote

class NamespaceSuite extends XmlQuoteSuite {

  test("reconstruct not prefixed namespaced elem") {
    assert(xml"""<foo xmlns="uri"/>""" ≈ <foo xmlns="uri"/>)
  }

  test("reconstruct namespaced elem") {
    assert(xml"""<foo xmlns:pre="uri"/>""" ≈ <foo xmlns:pre="uri"/>)
  }

  test("reconstruct multi-namespaced elem") {
    assert(xml"""<foo xmlns:a="uri1" xmlns:b="uri2"/>""" ≈ <foo xmlns:a="uri1" xmlns:b="uri2"/>)
  }

  test("reconstruct nested namespaced elem") {
    assert(xml"""<foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>""" ≈ <foo xmlns:pre1="uri1"><bar xmlns:pre2="uri2"/></foo>)
  }

  test("reconstruct shadowed namespaced elem") {
    assert(xml"""<foo xmlns:pre="a"><bar xmlns:pre="b"/></foo>""" ≈ <foo xmlns:pre="a"><bar xmlns:pre="b"/></foo>)
  }

  test("reconstruct nested unquoted elems") {
    assert(xml"""<a xmlns:pre="scope0">${ xml"<b/>" }</a>""" ≈
      <a xmlns:pre="scope0">{ <b/> }</a>)

    assert(xml"""<a xmlns:s0="s0">${ xml"""<b xmlns:s1="s1"><c/></b>""" }</a>""" ≈
      <a xmlns:s0="s0">{ <b xmlns:s1="s1"><c/></b> }</a>)

    val b = <b/>
    assert(xml"""<a xmlns:pre="scope0">${ xml"<b/>" }</a>""" !≈
      xml"""<a xmlns:pre="scope0">$b</a>""")

    val _ = xml"""<a xmlns="1">${ () => xml"<b/>" }</a>""" // should compile
  }

  test("invalid namespace") {
    " xml\"\"\"<a xmlns=\"&a;&b;\" />\"\"\" " shouldNot typeCheck
  }
}
