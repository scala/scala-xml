package scala.xml
package factory

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.{ Properties => PropertiesFor }
import org.scalacheck.Prop.AnyOperators

import scala.language.implicitConversions
import org.scalacheck.util.Pretty

object XMLLoaderSpec extends PropertiesFor("factory.XMLLoader")
    with DocumentGen
    with NodeGen
    with dtd.DocTypeGen
    with XmlStringGen {

  def testParser: SAXParser = {
    val parser = XML.parser
    val reader = parser.getXMLReader
    // Disable loading of external DTD files, to suppress
    // java.io.FileNotFoundException: "dtd" (No such file or directory)
    reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    parser
  }

  val loader: XMLLoader[Elem] =
    new XMLLoader[Elem] {
      override def parser = testParser
    }

  val genXMLLoader: Gen[XMLLoader[Elem]] =
    Gen.const(
      loader
    )

  class StreamAndWriter(
    val stream: java.io.ByteArrayOutputStream,
    val writer: java.io.Writer
  )

  val genStreamAndWriter: Gen[StreamAndWriter] = for {
    out <- Gen.delay(new java.io.ByteArrayOutputStream)
  } yield {
    new StreamAndWriter(
      out,
      new java.io.OutputStreamWriter(
        out,
        "UTF-8" // java.nio.charset.StandardCharsets.UTF_8.name
      )
    )
  }

  implicit val arbStreamAndWriter = Arbitrary {
    genStreamAndWriter
  }

  val genInputStream: Gen[java.io.InputStream] = for {
    inOut <- Arbitrary.arbitrary[StreamAndWriter]
    document <- Arbitrary.arbitrary[Document]
    encoding <- Gen.const("UTF-8") // java.nio.charset.StandardCharsets.UTF_8.name
    xmlDecl <- Gen.const(true) // Gen.oneOf(true, false)
    doctype <- Arbitrary.arbitrary[dtd.DocType]
    minimizeTags <- Gen.oneOf(
      MinimizeMode.Default,
      MinimizeMode.Always,
      MinimizeMode.Never
    )
  } yield {
    XML.write(
      inOut.writer,
      Group(document.children ++ Seq(document.docElem)),
      encoding,
      xmlDecl,
      doctype.copy(name = document.docElem.nameToString(new StringBuilder).toString),
      minimizeTags
    )
    inOut.writer.flush()
    val str = inOut.stream.toString
    new java.io.StringBufferInputStream(str)
  }

  implicit val arbInputStream = Arbitrary {
    genInputStream
  }

  val genInputSource: Gen[InputSource] = for {
    is <- Arbitrary.arbitrary[java.io.InputStream]
  } yield {
    Source.fromInputStream(is)
  }

  implicit val arbInputSource = Arbitrary {
    genInputSource
  }

  property("adapter") = {
    XML.adapter ne XML.adapter
  }

  property("parser") = {
    XML.parser ne XML.parser
  }

  implicit def prettyInputStream(is: java.io.InputStream) = Pretty { p =>
    val builder = new StringBuilder
    is.reset()
    var off = 0
    val chunkSize = 65536
    val arr = new Array[Byte](chunkSize)
    while (is.available() > 0) {
      val len = scala.math.min(is.available(), chunkSize)
      is.read(arr, off, chunkSize)
      builder ++= new String(arr.take(len))
      off += len
    }
    builder.toString
  }

  implicit def prettyInputSource(is: InputSource) = 
    prettyInputStream(is.getByteStream)

  // FIXME: xerces.internal.impl.io.MalformedByteSequenceException:
  // Invalid byte 1 of 1-byte UTF-8 sequence.
  // property("load(_: java.io.InputStream)") = {
  //   Prop.forAll { is: java.io.InputStream =>
  //     loader.load(is)
  //     Prop.passed
  //   }
  // }

  // FIXME: xerces.internal.impl.io.MalformedByteSequenceException:
  // Invalid byte 1 of 1-byte UTF-8 sequence.
  // property("load(_: InputSource)") = {
  //   Prop.forAll { is: InputSource =>
  //     loader.load(is)
  //     Prop.passed
  //   }
  // }

  property("loadString(_: String)") = {
    // Use forAllNoShrink since Scalacheck's shrinking strategy for a
    // string, removing characters byte-by-byte, won't improve the
    // insight of a failure.  Scalacheck is getting false feedback
    // while shrinking since Xerces only throws a SAXParseException,
    // albeit with different message values, but Scalacheck doesn't
    // listen to them.
    Prop.forAllNoShrink(genXmlString) { xml: String =>
      loader.loadString(xml)
      Prop.passed
    }
  }

  property("loadString(\"\").throws[Exception]") = {
    Prop.throws(classOf[org.xml.sax.SAXParseException]) {
      loader.loadString("")
    }
  }

  property("loadString(<?xml version='1.0'?>).throws[Exception]") = {
    Prop.throws(classOf[org.xml.sax.SAXParseException]) {
      loader.loadString("<?xml version='1.0'?>")
    }
  }

  property("loadString(</>).throws[Exception]") = {
    Prop.throws(classOf[org.xml.sax.SAXParseException]) {
      loader.loadString("</>").toString ?= "</>"
    }
  }

  property("loadString(<_/>)") = {
    val xml = <_/>
    loader.loadString(xml.toString) ?= xml
  }

  property("loadString(<_:_/>)") = {
    val xml = <_:_/>
    loader.loadString(xml.toString) ?= xml
  }

  property("loadString(<_:_></_:_>)") = {
    val xml = <_:_></_:_>
    loader.loadString(xml.toString) ?= xml
  }

  property("loadString(<!----><_/>)") = {
    loader.loadString("<!----><_/>") ?= <_/>
  }

  // Confirm external DTD files are not loaded, see above.
  property("loadString(<!DOCTYPE _ SYSTEM 'dtd'><_/>)") = {
    loader.loadString("<!DOCTYPE _ SYSTEM 'dtd'><_/>") ?= <_/>
  }

  property("loadString(<!DOCTYPE _:_ ><_:_/>)") = {
    val xml = """<!DOCTYPE _ >
                |<_:_/>""".stripMargin
    // val is = new java.io.StringBufferInputStream(xml)
    loader.loadString(xml) ?= <_:_/>
  }
}
