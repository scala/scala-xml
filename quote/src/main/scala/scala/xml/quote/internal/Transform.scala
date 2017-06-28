package scala.xml.quote.internal

import ast._
import scala.collection.mutable.ListBuffer

/** Apply transformations and validity checks to the xml tree.
  */
private[internal] trait Transform { self: QuoteImpl =>

  def transform(nodes: Seq[Node]): Seq[Node] =
    nodes.map(transform)

  def transform(node: Node): Node = node match {
    case elem: Elem =>
      validateAttributes(elem.attributes)

      var children = elem.children.map(transform)
      if (XmlSettings.isCoalescing) children = coalesce(children)

      val isGroup = elem.name == "xml:group" && !elem.minimizeEmpty // <xml:group/> is Elem in scalac
      if (isGroup) Group(children, elem.pos)
      else elem.copy(children = children)

    case _ =>
      node
  }

  /** Merge text sections */
  private def coalesce(nodes: Seq[Node]): Seq[Node] = {
    val buf = new ListBuffer[Node]
    val sb = new StringBuilder
    var pos = -1

    def purgeText() = {
      if (sb.nonEmpty) {
        buf += Text(sb.result(), pos)
        pos = -1
        sb.clear()
      }
    }

    def setPos(newPos: Position) = {
      if (pos < 0) pos = newPos
    }

    nodes.foreach {
      case Text(text, pos) =>
        setPos(pos)
        sb ++= text
      case PCData(data, pos) =>
        setPos(pos)
        sb ++= data
      case n =>
        purgeText()
        buf += n
    }

    purgeText()
    buf.toList
  }

  import self.c.{Type, typeOf}
  private val StringTpe = typeOf[String]
  private val SeqOfNodeTpe = typeOf[Seq[scala.xml.Node]]
  private val OptionOfSeqOfNodeTpe = typeOf[Option[Seq[scala.xml.Node]]]

  private def validateAttributes(atts: Seq[Attribute]): Unit = {
    val duplicates = atts
      .groupBy(_.name)
      .collect { case (_, as) if as.size > 1 => as.head }

    duplicates.foreach { dup =>
      val msg = s"attribute ${dup.name} may only be defined once"
      self.abort(dup.pos, msg)
    }

    // constructors overload resolution
    atts.foreach { att =>
      att.value match {
        case Seq(p: Placeholder) =>
          val expected =
            if (att.isNamespace) Seq(StringTpe)
            else Seq(StringTpe, SeqOfNodeTpe, OptionOfSeqOfNodeTpe)
          typeCheck(p, expected)

        case nodeSeq if nodeSeq.size > 1 && att.isNamespace =>
          typeMismatch(nodeSeq.head.pos, "scala.xml.NodeBuffer", "String")

        case _ =>
      }
    }
  }

  private def typeCheck(p: Placeholder, expected: Seq[Type]): Unit = {
    val tpe = self.arg(p.id).tpe

    if (!expected.exists(tpe <:< _)) {
      typeMismatch(p.pos, tpe.toString, expected.mkString(" | "))
    }
  }

  private def typeMismatch(pos: Position, found: String, required: String): Unit = {
    val msg =
      s"""type mismatch;
         | found   : $found
         | required: $required
       """.stripMargin
    self.abort(pos, msg)
  }
}
