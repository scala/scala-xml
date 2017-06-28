package scala.xml.quote.internal

import scala.collection.mutable.ListBuffer

private[internal] object ast {

  type Position = Int
  trait Positioned {
    def pos: Position
  }

  abstract class Node extends Positioned

  /** <xml:group><node1/><node2/></xml:group> */
  final case class Group(nodes: Seq[Node], pos: Position) extends Node

  /** <prefix:label attributeKey="attributeValue">
    *   <child1/>
    *   <child2/>
    * </prefix:label>
    */
  final case class Elem(name: String,
                        attributes: Seq[Attribute],
                        minimizeEmpty: Boolean,
                        children: Seq[Node],
                        pos: Position) extends Node {
    def prefix: String = name.take(prefixEnd)
    def label: String = name.drop(prefixEnd + 1)
    private def prefixEnd = name.indexOf(':')
  }

  /** <foo>text</foo> */
  final case class Text(text: String, pos: Position) extends Node

  final case class Placeholder(id: Int, pos: Position) extends Node

  /** <!--commentText--> */
  final case class Comment(text: String, pos: Position) extends Node

  /** <![CDATA[data]]> */
  final case class PCData(data: String, pos: Position) extends Node

  /** <?target proctext?> */
  final case class ProcInstr(target: String, proctext: String, pos: Position) extends Node

  /** <xml:unparsed>data</xml:unparsed> */
  final case class Unparsed(data: String, pos: Position) extends Node

  /** <foo>&entityName;</foo> */
  final case class EntityRef(name: String, pos: Position) extends Node

  /** <foo name="value" />
    *
    * @param value is `Text` or `{scalaExpression}`
    */
  final case class Attribute(name: String, value: Seq[Node], pos: Position) extends Positioned {
    def prefix: String = name.take(prefixEnd)
    def key: String = name.drop(prefixEnd + 1)
    // wrong but like scalac (e.g. xmlnsfoo is a namespace)
    def isNamespace = name.startsWith("xmlns")
    private def prefixEnd = name.indexOf(':')
  }

  object Attribute {

    type AttValue = Either[String, Placeholder]

    def apply(name: String, value0: AttValue, pos: Position): Attribute = {
      val value = value0 match {
        case Left(s)  => normalizeAttValue(s, pos)
        case Right(p) => Seq(p)
      }
      Attribute(name, value, pos)
    }

    /** Replaces character and entity references */
    private def normalizeAttValue(value: String, pos0: Position): Seq[Node] = {
      def ref(it : Iterator[Char]) = it.takeWhile(_ != ';').mkString

      val it = value.iterator.buffered
      val buf = new ListBuffer[Node]
      val sb = new StringBuilder
      var pos = pos0

      def purgeText() = {
        if (sb.nonEmpty) {
          buf += Text(sb.result(), pos)
          sb.clear()
        }
      }

      while (it.hasNext) { pos += 1; it.next() } match {
        case ' ' | '\t' | '\n' | '\r' =>
          sb += ' '

        case '&' if it.head == '#' =>
          it.next()
          val radix =
            if (it.head == 'x') { it.next(); 16 }
            else 10
          sb += XmlParser.charValueOf(ref(it), radix)

        case '&' =>
          val name = ref(it)
          attrUnescape.get(name) match {
            case Some(c) =>
              sb += c
            case _ =>
              purgeText()
              buf += EntityRef(name, pos)
          }

        case c =>
          sb += c

      }

      purgeText()
      buf.result()
    }

    private val attrUnescape = Map(
      "lt"    -> '<',
      "gt"    -> '>',
      "apos"  -> '\'',
      "quot"  -> '"',
      "quote" -> '"'
    )
  }
}
