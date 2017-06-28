package scala.xml.quote

class UnquoteSuite extends XmlQuoteSuite {

  test("unquote within elem") {
    assert(xml"<foo>${2}</foo>" ≈ <foo>{2}</foo>)
    assert(xml"<foo>${"bar"}</foo>" ≈ <foo>{"bar"}</foo>)

    assert(xml"<foo>1</foo>" !≈ xml"<foo>${1}</foo>")

    assert(xml"<a>${1}${2}</a>" ≈ <a>{1}{2}</a>)

    assert(xml"<foo>${<bar/>}</foo>" ≈ <foo>{<bar/>}</foo>)
    assert(xml"<foo>${<bar/><bat/>}</foo>" ≈ <foo>{<bar/><bat/>}</foo>)
  }

  test("unquote within attribute") {
    assert(xml"<foo a=${"foo"}/>" ≈ <foo a={"foo"}/>)
    assert(xml"<foo a=${<bar/>}/>" ≈ <foo a={<bar/>}/>)
    assert(xml"<foo a=${<bar/><bat/>}/>" ≈ <foo a={<bar/><bat/>}/>)
    assert(xml"<a b=${None}/>" ≈ <a b={None}/>)

    """ xml"<a b=${1} />" """ shouldNot typeCheck
  }

  test("unquote iterable") {
    assert(xml"<a>${ List(1, 2) }</a>" ≈ <a>{ List(1, 2) }</a>)
  }

  test("nested unquote") {
    assert(xml"<a>${xml"<b>${1}</b>"}</a>" ≈ <a>{<b>{1}</b>}</a>)
  }

  test("unquote unit") {
    assert(xml"<a>${}</a>" ≈ <a>{}</a>)
  }

  test("unquote within namespace") {
    assert(xml"<foo xmlns=${"foo"}/>" ≈ <foo xmlns={"foo"}/>)
    
    """ xml"<a xmlns=${<b/>} />" """ shouldNot typeCheck
    """ xml"<a xmlns=${None} />" """ shouldNot typeCheck
  }
}
