package scala.xml
package dtd.impl

// import org.scalacheck.Arbitrary
// import org.scalacheck.Gen

trait RegExpGen { // extends WordExp {

  // type _labelT = dtd.ContentModel.ElemName
  // type _regexpT = RegExp

  // val genLabel: Gen[_labelT] = for {
  //   name <- Arbitrary.arbitrary[String]
  // } yield {
  //   dtd.ContentModel.ElemName(name)
  // }

  // implicit val arbLabel = Arbitrary {
  //   genLabel
  // }

  // val genLetter: Gen[_regexpT] = for {
  //   label <- Arbitrary.arbitrary[_labelT]
  // } yield {
  //   Letter(label)
  // }

  // val genRegExp: Gen[_regexpT] =
  //   Gen.oneOf(genLetter, Gen.const(Wildcard()))

  // implicit val arbRegExp = Arbitrary {
  //   genRegExp
  // }
}
