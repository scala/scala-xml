package scala.xml

import org.scalacheck.Prop
import org.scalacheck.{ Properties => CheckProperties }

object Utf8StringSpec extends CheckProperties("Utf8String")
    with Utf8StringGen {

  // If character classes change, this will let you know.
  property("getType") = {
    Prop.forAll { c: Char =>
      val typ: String = Character.getType(c) match {
        case Character.COMBINING_SPACING_MARK => "COMBINING_SPACING_MARK"
        case Character.CONNECTOR_PUNCTUATION => "CONNECTOR_PUNCTUATION"
        case Character.CONTROL => "CONTROL"
        case Character.CURRENCY_SYMBOL => "CURRENCY_SYMBOL"
        case Character.DASH_PUNCTUATION => "DASH_PUNCTUATION"
        case Character.DECIMAL_DIGIT_NUMBER => "DECIMAL_DIGIT_NUMBER"
        case Character.ENCLOSING_MARK => "ENCLOSING_MARK"
        case Character.END_PUNCTUATION => "END_PUNCTUATION"
        case Character.FINAL_QUOTE_PUNCTUATION => "FINAL_QUOTE_PUNCTUATION"
        case Character.FORMAT => "FORMAT"
        case Character.INITIAL_QUOTE_PUNCTUATION => "INITIAL_QUOTE_PUNCTUATION"
        case Character.LETTER_NUMBER => "LETTER_NUMBER"
        case Character.LINE_SEPARATOR => "LINE_SEPARATOR"
        case Character.LOWERCASE_LETTER => "LOWERCASE_LETTER"
        case Character.MATH_SYMBOL => "MATH_SYMBOL"
        case Character.MODIFIER_LETTER => "MODIFIER_LETTER"
        case Character.MODIFIER_SYMBOL => "MODIFIER_SYMBOL"
        case Character.NON_SPACING_MARK => "NON_SPACING_MARK"
        case Character.OTHER_LETTER => "OTHER_LETTER"
        case Character.OTHER_NUMBER => "OTHER_NUMBER"
        case Character.OTHER_PUNCTUATION => "OTHER_PUNCTUATION"
        case Character.OTHER_SYMBOL => "OTHER_SYMBOL"
        case Character.PARAGRAPH_SEPARATOR => "PARAGRAPH_SEPARATOR"
        case Character.PRIVATE_USE => "PRIVATE_USE"
        case Character.SPACE_SEPARATOR => "SPACE_SEPARATOR"
        case Character.START_PUNCTUATION => "START_PUNCTUATION"
        case Character.SURROGATE => "SURROGATE"
        case Character.TITLECASE_LETTER => "TITLECASE_LETTER"
        case Character.UNASSIGNED => "UNASSIGNED"
        case Character.UPPERCASE_LETTER => "UPPERCASE_LETTER"
      }
      Prop.collect(s"Character.$typ") {
        Prop.passed
      }
    }
  }
}
