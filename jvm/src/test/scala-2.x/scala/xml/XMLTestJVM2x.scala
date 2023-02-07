package scala.xml

import org.junit.{Test => UnitTest}
import org.junit.Assert.assertEquals
import scala.xml.parsing.ConstructingParser

class XMLTestJVM2x {
  @UnitTest
  def t2354(): Unit = {
    val xml_good: String = "<title><![CDATA[Hello [tag]]]></title>"
    val xml_bad: String = "<title><![CDATA[Hello [tag] ]]></title>"

    val parser1: ConstructingParser = ConstructingParser.fromSource(io.Source.fromString(xml_good), preserveWS = false)
    val parser2: ConstructingParser = ConstructingParser.fromSource(io.Source.fromString(xml_bad), preserveWS = false)

    parser1.document()
    parser2.document()
  }

  @UnitTest
  def t8253(): Unit = {
    // `identity(foo)` used to match the overly permissive match in SymbolXMLBuilder
    // which was intended to more specifically match `_root_.scala.xml.Text(...)`

    import reflect.runtime.universe._ // not using the XML library in compiler tests

    val ns1: String = "ns1"
    assertEquals(reify(ns1).tree.toString, q"ns1".toString)
    assertEquals("<sample xmlns='ns1'/>",
      """|{
         |  var $tmpscope: _root_.scala.xml.NamespaceBinding = $scope;
         |  $tmpscope = new _root_.scala.xml.NamespaceBinding(null, "ns1", $tmpscope);
         |  {
         |    val $scope: _root_.scala.xml.NamespaceBinding = $tmpscope;
         |    new _root_.scala.xml.Elem(null, "sample", _root_.scala.xml.Null, $scope, true)
         |  }
         |}""".stripMargin,
      q"<sample xmlns='ns1'/>".toString)
    assertEquals("<sample xmlns={identity(ns1)}/>",
      """|{
         |  var $tmpscope: _root_.scala.xml.NamespaceBinding = $scope;
         |  $tmpscope = new _root_.scala.xml.NamespaceBinding(null, ns1, $tmpscope);
         |  {
         |    val $scope: _root_.scala.xml.NamespaceBinding = $tmpscope;
         |    new _root_.scala.xml.Elem(null, "sample", _root_.scala.xml.Null, $scope, true)
         |  }
         |}""".stripMargin,
      q"<sample xmlns={ns1}/>".toString)
    assertEquals("<sample xmlns:foo='ns1'/>",
      """|{
         |  var $tmpscope: _root_.scala.xml.NamespaceBinding = $scope;
         |  $tmpscope = new _root_.scala.xml.NamespaceBinding("foo", "ns1", $tmpscope);
         |  {
         |    val $scope: _root_.scala.xml.NamespaceBinding = $tmpscope;
         |    new _root_.scala.xml.Elem(null, "sample", _root_.scala.xml.Null, $scope, true)
         |  }
         |}""".stripMargin,
      q"<sample xmlns:foo='ns1'/>".toString)
    assertEquals("<sample xmlns:foo={identity(ns1)}/>",
      """|{
         |  var $tmpscope: _root_.scala.xml.NamespaceBinding = $scope;
         |  $tmpscope = new _root_.scala.xml.NamespaceBinding("foo", ns1, $tmpscope);
         |  {
         |    val $scope: _root_.scala.xml.NamespaceBinding = $tmpscope;
         |    new _root_.scala.xml.Elem(null, "sample", _root_.scala.xml.Null, $scope, true)
         |  }
         |}""".stripMargin,
      q"<sample xmlns:foo={ns1}/>".toString)
  }

  @UnitTest
  def t8466lift(): Unit = {
    import scala.reflect.runtime.universe._

    implicit val liftXmlComment: Liftable[Comment] = Liftable[Comment] { comment =>
      q"new _root_.scala.xml.Comment(${comment.commentText})"
    }
    liftXmlComment(Comment("foo"))
    assertEquals(q"${Comment("foo")}".toString, q"<!--foo-->".toString)
  }

  @UnitTest
  def t8466unlift(): Unit = {
    import scala.reflect.runtime.universe._

    implicit val unliftXmlComment: Unliftable[Comment] = Unliftable[Comment] {
      case q"new _root_.scala.xml.Comment(${value: String})" => Comment(value)
    }
    unliftXmlComment.unapply(q"<!--foo-->")
    val q"${comment: Comment}" = q"<!--foo-->"
    assertEquals(comment.commentText, "foo")
  }

  @UnitTest
  def t9027(): Unit = {
    // used to be parsed as .println

    import reflect.runtime._, universe._

    assertEquals(
      """|{
         |  {
         |    val $buf = new _root_.scala.xml.NodeBuffer();
         |    $buf.$amp$plus(new _root_.scala.xml.Elem(null, "a", _root_.scala.xml.Null, $scope, true));
         |    $buf.$amp$plus(new _root_.scala.xml.Elem(null, "b", _root_.scala.xml.Null, $scope, true));
         |    $buf
         |  };
         |  println("hello, world.")
         |}""".stripMargin,
      q"""<a/><b/>
          println("hello, world.")""".toString)
    assertEquals(
      """|{
         |  {
         |    val $buf = new _root_.scala.xml.NodeBuffer();
         |    $buf.$amp$plus(new _root_.scala.xml.Elem(null, "a", _root_.scala.xml.Null, $scope, true));
         |    $buf.$amp$plus(new _root_.scala.xml.Elem(null, "b", _root_.scala.xml.Null, $scope, true));
         |    $buf.$amp$plus(new _root_.scala.xml.Elem(null, "c", _root_.scala.xml.Null, $scope, true));
         |    $buf
         |  };
         |  println("hello, world.")
         |}""".stripMargin,
      q"""<a/>
      <b/>
      <c/>
      println("hello, world.")""".toString)
  }
}
