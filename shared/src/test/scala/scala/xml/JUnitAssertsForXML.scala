package scala.xml

object JUnitAssertsForXML {

  private[xml] def assertEquals(expected: String, actual: NodeSeq): Unit =
    org.junit.Assert.assertEquals(expected, actual.toString)
}
