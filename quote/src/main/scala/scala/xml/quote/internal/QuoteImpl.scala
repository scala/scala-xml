package scala.xml.quote.internal

import fastparse.all._

import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.whitebox
import scala.xml.quote.internal.QuoteImpl._

class QuoteImpl(val c: whitebox.Context) extends Liftables with Transform {
  import c.universe._

  private lazy val q"$_($_(..$parts)).xml.apply[..$_](..$args)" = c.macroApplication

  def apply[T](args: Tree*): Tree = {
    val nodes = transform(parsedXml)
    lift(nodes)
  }

  private[internal] def arg(i: Int): Tree = args(i)

  private[internal] def abort(offset: Int, msg: String): Nothing = {
    val pos = correspondingPosition(offset)
    c.abort(pos, msg)
  }

  private lazy val (xmlStr, offsets) = {
    val sb = new StringBuilder
    val poss = ArrayBuffer.empty[Int]

    def appendPart(part: Tree) = {
      val q"${value: String}" = part
      poss += sb.length
      sb ++= value
      poss += sb.length
    }

    def appendHole(i: Int) =
      sb ++= Hole.encode(i)

    for ((part, i) <- parts.init.zipWithIndex) {
      appendPart(part)
      appendHole(i)
    }
    appendPart(parts.last)

    (sb.toString, poss.toArray)
  }

  /** Given an offset in the xmlString computes the corresponding position */
  private def correspondingPosition(offset: Int): Position = {
    val index = offsets.lastIndexWhere(offset >= _)
    val isWithinHoleOrAtTheEnd = index % 2 != 0

    if (isWithinHoleOrAtTheEnd) {
      val prevPartIndex = (index - 1) / 2
      val pos = parts(prevPartIndex).pos
      val posOffset = offset - offsets(index - 1)
      pos.withPoint(pos.point + posOffset)
    } else {
      val partIndex = index / 2
      val pos = parts(partIndex).pos
      val posOffset = offset - offsets(index)
      pos.withPoint(pos.point + posOffset)
    }
  }

  private def parsedXml: Seq[ast.Node] = {
    xmlParser.XmlExpr.parse(xmlStr) match {
      case Parsed.Success(nodes, _) => nodes
      case Parsed.Failure(expected, offset, _) =>
        abort(offset, s"expected: $expected")
    }
  }

  def pp[T <: Tree](t: T): T = {
    println(showCode(t, printIds = true))
    t
  }
}

private object QuoteImpl {
  val xmlParser = {
    val Placeholder = P( Index ~ Hole.Parser ).map { case (pos, id) => ast.Placeholder(id, pos) }
    new XmlParser(Placeholder)
  }
}
