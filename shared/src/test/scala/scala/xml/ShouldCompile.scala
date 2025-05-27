package scala.xml

// these tests depend on xml, so they ended up here,
// though really they are compiler tests

import scala.collection._
import scala.collection.mutable.ArrayBuffer

// t1761
class Foo {
  val elements: Seq[Node] = Nil
  val innerTransform: PartialFunction[Elem, String] = {
    case Elem(_, l: String, _, _, _@ _*) if elements.exists(_.label == l) =>
      l
  }
}

// t2281
class t2281A {
  def f(x: Boolean): Seq[Node] = if (x) <br/><br/> else <br/>
}

class t2281B {
  def splitSentences(text: String): ArrayBuffer[String] = {
    val outarr: ArrayBuffer[String] = new ArrayBuffer[String]
    var outstr: StringBuffer = new StringBuffer
    var prevspace: Boolean = false
    val ctext: String = text.replaceAll("\n+", "\n")
    ctext.foreach { c =>
      outstr.append(c)
      if (c == '.' || c == '!' || c == '?' || c == '\n' || c == ':' || c == ';' || (prevspace && c == '-')) {
        outarr += outstr.toString
        outstr = new StringBuffer
      }
      if (c == '\n') {
        outarr += "\n\n"
      }
      prevspace = c == ' '
    }
    if (outstr.length > 0) {
      outarr += outstr.toString
    }
    outarr
  }

  def spanForSentence(x: String, picktext: String): Seq[Node] =
    if (x == "\n\n") {
      <br/><br/>
    } else {
      <span class='clicksentence' style={ if (x == picktext) "background-color: yellow" else "" }>{ x }</span>
    }

  def selectableSentences(text: String, picktext: String): ArrayBuffer[Seq[Node]] = {
    val sentences: ArrayBuffer[String] = splitSentences(text)
    sentences.map(x => spanForSentence(x, picktext))
  }
}

// SI-5858
object SI_5858 {
  new Elem(null, null, Null, TopScope, true, Nil: _*) // was ambiguous
}

class Floozy {
  def fooz(x: Node => String): Unit = ()
  def foo(m: Node): Unit = fooz {
    case Elem(_, _, _, _, n, _*) if n == m => "gaga"
  }
}

object guardedMatch { // SI-3705
  // guard caused verifyerror in oldpatmat -- TODO: move this to compiler test suite
  def updateNodes(ns: ScalaVersionSpecific.SeqOfNode): ScalaVersionSpecific.SeqOfNode =
    for (subnode <- ns) yield subnode match {
      case <d>{ _ }</d> if true => <d>abc</d>
      case Elem(prefix, label, attribs, scope, children @ _*) =>
        Elem(prefix, label, attribs, scope, minimizeEmpty = true, updateNodes(children): _*)
      case other => other
    }
  updateNodes(<b/>)
}

// SI-6897
object shouldCompile {
  val html: Seq[Node] = (null: Any) match {
    case 1 => <xml:group></xml:group>
    case 2 => <p></p>
  }
}
