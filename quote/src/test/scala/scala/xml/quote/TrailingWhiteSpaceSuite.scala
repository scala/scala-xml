package scala.xml.quote

class TrailingWhiteSpaceSuite extends XmlQuoteSuite {

  test("discard outer trailing whitespace") {
    assert(xml" <a/>" ≈ <a/>)
    assert(xml"<a/> " ≈ <a/>)
    assert(xml" <a/> " ≈ <a/>)
  }

  test("keep trailing whitespaces in elem content") {
    assert(xml"<a>  </a>" ≈ <a>  </a>)
    assert(xml"<a> <b/> </a>" ≈ <a> <b/> </a>)
  }

  test("reconstruct multiline element") {
    val xml1 = xml"""
      <a>
        <b/>
      </a>
    """

    val xml2 =
      <a>
        <b/>
      </a>

    assert(xml1 ≈ xml2)
  }
}
