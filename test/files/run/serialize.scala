object Test {
  def main(args: Array[String]): Unit = {
    Test4_xml
  }
}

//############################################################################
// Serialization
//############################################################################

object Serialize {
  @throws(classOf[java.io.IOException])
  def write[A](o: A): Array[Byte] = {
    val ba = new java.io.ByteArrayOutputStream(512)
    val out = new java.io.ObjectOutputStream(ba)
    out.writeObject(o)
    out.close()
    ba.toByteArray()
  }
  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  def read[A](buffer: Array[Byte]): A = {
    val in =
      new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(buffer))
    in.readObject().asInstanceOf[A]
  }
  def check[A, B](x: A, y: B) {
    println("x = " + x)
    println("y = " + y)
    println("x equals y: " + (x equals y) + ", y equals x: " + (y equals x))
    assert((x equals y) && (y equals x))
    println()
  }
}
import Serialize._

//############################################################################
// Test classes in package "scala.xml"

object Test4_xml {
  import scala.xml.{Attribute, Document, Elem, Null, PrefixedAttribute, Text}

  case class Person(name: String, age: Int)

  try {
    // Attribute
    val a1 = new PrefixedAttribute("xml", "src", Text("hello"), Null)
    val _a1: Attribute = read(write(a1))
    check(a1, _a1)

    // Document
    val d1 = new Document
    d1.docElem = <title></title>
    d1.encoding = Some("UTF-8")
    val _d1: Document = read(write(d1))
    check(d1, _d1)

    // Elem
    val e1 = <html><title>title</title><body></body></html>;
    val _e1: Elem = read(write(e1))
    check(e1, _e1)

    class AddressBook(a: Person*) {
      private val people: List[Person] = a.toList
      def toXHTML =
      <table cellpadding="2" cellspacing="0">
        <tr>
          <th>Last Name</th>
          <th>First Name</th>
        </tr>
        { for (p <- people) yield
        <tr>
          <td> { p.name } </td>
          <td> { p.age.toString() } </td>
        </tr> }
      </table>;
    }

    val people = new AddressBook(
      Person("Tom", 20),
      Person("Bob", 22),
      Person("James", 19))

    val e2 =
      <html>
      <body>
        { people.toXHTML }
      </body>
      </html>;
    val _e2: Elem = read(write(e2))
    check(e2, _e2)
  }
  catch {
    case e: Exception =>
      println("Error in Test4_xml: " + e)
      throw e
  }
}