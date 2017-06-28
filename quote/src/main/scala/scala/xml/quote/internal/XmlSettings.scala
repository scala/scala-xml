package scala.xml.quote.internal

private[internal] object XmlSettings {

  /** Convert PCData to Text and coalesce sibling nodes
    *
    * Coalescing defaults to false from scala 2.12.
    * See [[https://github.com/scala/scala/commit/be9450b2cffe3b1ee723fc7e2f5df83644b35a66]]
    */
  def isCoalescing: Boolean =
    util.Properties.versionNumberString < "2.12"
}
