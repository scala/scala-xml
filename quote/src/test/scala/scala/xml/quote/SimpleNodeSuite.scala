package scala.xml.quote

class SimpleNodeSuite extends XmlQuoteSuite {

  test("reconstruct sequence of nodes") {
    assert(xml"<foo/><bar/>" ≈ <foo/><bar/>)
  }

  test("reconstruct minimized elem") {
    assert(xml"<foo/>" ≈ <foo/>)
  }

  test("reconstruct maximized elem") {
    assert(xml"<foo></foo>" ≈ <foo></foo>)
  }

  test("reconstruct prefixed elem") {
    assert(xml"<foo:bar/>" ≈ <foo:bar/>)
  }

  test("reconstruct nested elem") {
    assert(xml"<foo><bar/></foo>" ≈ <foo><bar/></foo>)
  }

  test("reconstruct elem with unprefixed attributes") {
    assert(xml"""<foo a="a" b="b"/>""" ≈ <foo a="a" b="b"/>)
  }

  test("reconstruct elem with prefixed attributes") {
    assert(xml"""<foo a:a="a" b:b="b"/>""" ≈ <foo a:a="a" b:b="b"/>)
  }

  test("reconstruct elem with attributes") {
    assert(xml"""<foo a="'"/>""" ≈ <foo a="'"/>)
    assert(xml"""<foo a='"'/>""" ≈ <foo a='"'/>)
  }

  test("reconstruct Text") {
    assert(xml"<a>Hello</a>" ≈ <a>Hello</a>)
    assert(xml"<a>></a>" ≈ <a>></a>)
    assert(xml"<a>{</a>" ≈ <a>{{</a>)
    assert(xml"<a>}</a>" ≈ <a>}}</a>)
  }

  test("reconstruct EntityRef") {
    assert(xml"<foo>&name;</foo>" ≈ <foo>&name;</foo>)
    assert(xml"<foo>&lt;</foo>" ≈ <foo>&lt;</foo>)
    assert(xml"<foo>Hello &name;!</foo>" ≈ <foo>Hello &name;!</foo>)
    assert(xml"<foo>&na:me;</foo>" ≈ <foo>&na:me;</foo>)

    // In attribute position
    assert(xml"""<foo a="&name;" />""" ≈ <foo a="&name;" />)
    assert(xml"""<foo a="&na:me;" />""" ≈ <foo a="&na:me;" />)
    assert(xml"""<foo a="&lt;" />""" ≈ <foo a="&lt;" />)
    assert(xml"""<foo a="Hello &name;!"/>""" ≈ <foo a="Hello &name;!"/>)
    assert(xml"""<foo a="1 &lt; 2"/>""" ≈ <foo a="1 &lt; 2"/>)
  }

  test("reconstruct CharRef") {
    assert(xml"<foo>&#1234;</foo>" ≈ <foo>&#1234;</foo>)
    assert(xml"<foo>&#x1234;</foo>" ≈ <foo>&#x1234;</foo>)
    assert(xml"<foo>Hello&#x1234;Allan</foo>" ≈ <foo>Hello&#x1234;Allan</foo>)

    // In attribute position
    assert(xml"""<foo a="&#1234;" />""" ≈ <foo a="&#1234;" />)
    assert(xml"""<foo a="&#x1234;" />""" ≈ <foo a="&#x1234;" />)
    assert(xml"""<foo a="Hello&#x1234;Allan" />""" ≈ <foo a="Hello&#x1234;Allan" />)
  }

  test("reconstruct group") {
    assert(xml"<xml:group><foo/><bar/></xml:group>" ≈ <xml:group><foo/><bar/></xml:group>)
    assert(xml"<xml:group></xml:group>" ≈ <xml:group></xml:group>)
  }

  test("reconstruct Comment") {
    assert(xml"<!---->" ≈ <!---->)
    assert(xml"<!----->" ≈ <!----->)
    assert(xml"<!--foo-->" ≈ <!--foo-->)
    assert(xml"<!--a-b-->" ≈ <!--a-b-->)
  }

  test("reconstruct PCData") {
    assert(xml"<![CDATA[foo]]>" ≈ <![CDATA[foo]]>)
    assert(xml"<![CDATA[]]>" ≈ <![CDATA[]]>)

    assert(xml"<![CDATA[>]]>" ≈ <![CDATA[>]]>)
    assert(xml"<![CDATA[]>]]>" ≈ <![CDATA[]>]]>)
    assert(xml"<![CDATA[]]]]>" ≈ <![CDATA[]]]]>)
  }

  test("reconstruct ProcInstr") {
    assert(xml"<?foo bar?>" ≈ <?foo bar?>)
    assert(xml"<?foo?>" ≈ <?foo?>)
    assert(xml"<?foo     ?>" ≈ <?foo     ?>)
    assert(xml"<?foo   bar?>" ≈ <?foo   bar?>)
    assert(xml"<?foo??>" ≈ <?foo??>)
    assert(xml"<?foo<bar?>" ≈ <?foo<bar?>)
  }

  test("reconstruct unparsed") {
    assert(xml"<xml:unparsed>foo</xml:unparsed>" ≈ <xml:unparsed>foo</xml:unparsed>)
    assert(xml"<xml:unparsed>{</xml:unparsed>" ≈ <xml:unparsed>{</xml:unparsed>)
    assert(xml"<xml:unparsed><</xml:unparsed>" ≈ <xml:unparsed><</xml:unparsed>)
  }

  test("reconstruct coalescing elems") {
    assert(xml"<![CDATA[hello, world]]>" ≈ <![CDATA[hello, world]]>)

    assert(xml"<![CDATA[hello, world]]><![CDATA[hello, world]]>" ≈
      <![CDATA[hello, world]]><![CDATA[hello, world]]>)

    assert(xml"<foo>x<![CDATA[hello, world]]></foo>" ≈
      <foo>x<![CDATA[hello, world]]></foo>)

    assert(xml"<foo><![CDATA[hello, world]]></foo>" ≈
      <foo><![CDATA[hello, world]]></foo>)

    assert(xml"<foo><![CDATA[hello, world]]><![CDATA[hello, world]]></foo>" ≈
      <foo><![CDATA[hello, world]]><![CDATA[hello, world]]></foo>)

    assert(xml"<a><b/>start<![CDATA[hi & bye]]><c/>world<d/>stuff<![CDATA[red & black]]></a>" ≈
      <a><b/>start<![CDATA[hi & bye]]><c/>world<d/>stuff<![CDATA[red & black]]></a>)
    }
}
