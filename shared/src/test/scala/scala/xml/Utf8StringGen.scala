package scala.xml

import org.scalacheck.Arbitrary
import org.scalacheck.Gen

trait Utf8StringGen {
  val genUtf8String: Gen[String] =
    Arbitrary.arbitrary[String].map {
      _.filter { c =>
        // 1. SAXParseException: The entity name must immediately follow
        // the '&' in the entity reference
        // 2. SAXParseException: The content of elements must consist of
        // well-formed character data or markup.
        "<&".contains(c) == false
      }.filter { c =>
        Character.getType(c) match {
          case Character.COMBINING_SPACING_MARK |
            Character.CONNECTOR_PUNCTUATION     |
            // Character.CONTROL                |
            Character.CURRENCY_SYMBOL           |
            Character.DASH_PUNCTUATION          |
            Character.DECIMAL_DIGIT_NUMBER      |
            Character.ENCLOSING_MARK            |
            Character.END_PUNCTUATION           |
            Character.FINAL_QUOTE_PUNCTUATION   |
            // Character.FORMAT                 |
            Character.INITIAL_QUOTE_PUNCTUATION |
            Character.LETTER_NUMBER             |
            // Character.LINE_SEPARATOR         |
            Character.LOWERCASE_LETTER          |
            Character.MATH_SYMBOL               |
            Character.MODIFIER_LETTER           |
            Character.MODIFIER_SYMBOL           |
            Character.NON_SPACING_MARK          |
            Character.OTHER_LETTER              |
            Character.OTHER_NUMBER              |
            Character.OTHER_PUNCTUATION         |
            Character.OTHER_SYMBOL              |
            // Character.PARAGRAPH_SEPARATOR    |
            // Character.PRIVATE_USE            |
            Character.SPACE_SEPARATOR           |
            Character.START_PUNCTUATION         |
            // Character.SURROGATE              |
            Character.TITLECASE_LETTER          |
            // Character.UNASSIGNED             |
            Character.UPPERCASE_LETTER => true
          case _ => false
        }
      }
    }
}
