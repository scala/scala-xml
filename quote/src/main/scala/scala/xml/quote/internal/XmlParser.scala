package scala.xml.quote
package internal

import fastparse.all._

import scala.xml.parsing.TokenTests
import internal.{ast => p}

private[internal] class XmlParser(Hole: P[p.Placeholder]) extends TokenTests {
  import XmlParser._

  private val S = CharsWhile(isSpace).opaque("whitespace")

  val XmlExpr: P[Seq[p.Node]] = P( S.? ~ Xml.XmlContent.rep(min = 1, sep = S.?) ~ S.? ~ End )
  val XmlPattern: P[p.Node]   = P( S.? ~ Xml.ElemPattern ~ S.? ~ End )

  private[this] object Xml {

    val Elem: P[p.Node] = P( Index ~ TagHeader ~/ TagRest ).map {
      case (pos, (name, atts), children: Seq[p.Node @unchecked]) =>
        p.Elem(name, atts, minimizeEmpty = false, children, pos)
      case (pos, (name, atts), _) =>
        p.Elem(name, atts, minimizeEmpty = true, Nil, pos)
    }

    val TagHeader = P( "<" ~ Name ~/ (S ~ Attribute).rep ~/ S.? )
    val TagRest   = P( "/>" | ">" ~/ Content ~/ ETag ): P[Any] // P[Unit | Seq[p.Node]]
    val ETag      = P( "</" ~ Name ~ S.? ~ ">" ).toP0

//    // This parser respect tag's balance but reports wrong positions on failure
//    val Elem = P(
//      for {
//        pos          <- Index
//        (name, atts) <- TagHeader
//        children     <- TagRest(name)
//      } yield children match {
//        case cs: Seq[p.Node @unchecked] => p.Elem(name, atts, minimizeEmpty = false, cs, 0)
//        case _                          => p.Elem(name, atts, minimizeEmpty = false, Nil, 0)
//      }
//    )
//
//    val TagHeader             = P( "<" ~ Name ~/ (WL ~ Attribute).rep ~/ WL.? )
//    def TagRest(name: String) = P( "/>" | ">" ~/ Content ~/ ETag(name) ): P[Any] // P[Unit | Seq[p.Node]]
//    def ETag(name: String)    = P( "</" ~ name ~ WL.? ~ ">" )

    val Attribute = P( Index ~ Name ~/ Eq ~/ AttValue ).map {
      case (pos, name, value) => p.Attribute(name, value, pos)
    }
    val Eq       = P( S.? ~ "=" ~ S.? )
    val AttValue = P(
      "\"" ~/ (CharQ | Reference).rep.!.map(Left.apply) ~ "\"" |
      "'" ~/ (CharA | Reference).rep.!.map(Left.apply) ~ "'" |
      ScalaExpr.map(Right.apply)
    ): P[p.Attribute.AttValue]

    val Content               = P( (CharData | Reference | ScalaExpr | XmlContent).rep )
    val XmlContent: P[p.Node] = P( Unparsed | CDSect | PI | Comment | Elem )

    val ScalaExpr = Hole

    val Unparsed = P( Index ~ UnpStart ~/ UnpData.! ~ UnpEnd ).map { case (pos, data) => p.Unparsed(data, pos) }
    val UnpStart = P( "<xml:unparsed" ~ (S ~ Attribute).rep ~ S.? ~ ">" ).toP0
    val UnpEnd   = P( "</xml:unparsed>" )
    val UnpData  = P( (!UnpEnd ~ Char).rep )

    val CDSect  = P( Index ~ CDStart ~/ CData.! ~ CDEnd ).map { case (pos, data) => p.PCData(data, pos) }
    val CDStart = P( "<![CDATA[" )
    val CData   = P( (!"]]>" ~ Char).rep )
    val CDEnd   = P( "]]>" )

    val Comment = P( Index ~ "<!--" ~/ ComText.! ~ "-->" ).map { case (pos, text) => p.Comment(text, pos) }
    val ComText = P( (!"-->" ~ Char).rep )

    val PI = P( Index ~ "<?" ~ Name ~ S.? ~ PIProcText.! ~ "?>" ).map {
      case (pos, target, text) => p.ProcInstr(target, text, pos)
    }
    val PIProcText = P( (!"?>" ~ Char).rep )

    val Reference = P( EntityRef | CharRef )
    val EntityRef = P( Index ~ "&" ~ Name ~/ ";" ).map { case (pos, name) => p.EntityRef(name, pos) }
    val CharRef   = P( Index ~ ("&#" ~ Num ~ ";" | "&#x" ~ HexNum ~ ";") ).map {
      case (pos, cr) => p.Text(cr.toString, pos)
    }

    val Num    = P( CharIn('0' to '9').rep.! ).map(n => charValueOf(n))
    val HexNum = P( CharIn('0' to '9', 'a' to 'f', 'A' to 'F').rep.! ).map(n => charValueOf(n, 16))

    val CharData = P( Index ~ Char1.rep(1).! ).map { case (pos, text) => p.Text(text, pos) }

    val Char  = P( !Hole ~ AnyChar )
    val Char1 = P( !("<" | "&") ~ Char )
    val CharQ = P( !"\"" ~ Char1 )
    val CharA = P( !"'" ~ Char1 )

    val Name      = P( NameStart ~ NameChar.rep ).!.filter(_.last != ':').opaque("Name")
    val NameStart = P( CharPred(isNameStart) )
    val NameChar  = P( CharPred(isNameChar) )

    val ElemPattern: P[p.Node] = P( Index ~ TagPHeader ~ TagPRest ).map {
      case (pos, name, children: Seq[p.Node @unchecked]) =>
        p.Elem(name, Nil, minimizeEmpty = false, children, pos)
      case (pos, name, _) =>
        p.Elem(name, Nil, minimizeEmpty = true, Nil, pos)
    }

    val TagPHeader = P( "<" ~ Name ~/ S.?  )
    val TagPRest   = P( "/>" | ">" ~/ ContentP ~/ ETag ): P[Any] // P[Unit | Seq[p.Node]]

    val ContentP  = P( (ScalaPatterns | ElemPattern | CharDataP ).rep )
    // matches weirdness of scalac parser on xml reference.
    val CharDataP = P( Index ~ ("&" ~ CharData.? | CharData).! ).map { case (pos, text) => p.Text(text, pos) }

    val ScalaPatterns = ScalaExpr
  }
}


private[internal] object XmlParser {

  def charValueOf(cr: String, radix: Int = 10): Char =
    if (cr.isEmpty) 0.toChar
    else java.lang.Integer.parseInt(cr, radix).toChar

  private implicit class ParserOps[T](val self: P[T]) extends AnyVal {
    /** Discard the result of this parser */
    def toP0: P0 = self.map(_ => Unit)
  }
}
