package scala.xml

import java.io._
import java.util.Base64

object JavaByteSerialization {
  def roundTrip[T](obj: T): T = {
    deserialize[T](serialize[T](obj))
  }

  def serialize[T](in: T): Array[Byte] = {
    val bos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(bos)
    oos.writeObject(in)
    oos.flush()
    bos.toByteArray()
  }

  def deserialize[T](in: Array[Byte]): T = {
    val bis = new ByteArrayInputStream(in)
    val ois = new ObjectInputStream(bis)
    ois.readObject.asInstanceOf[T]
  }

  def base64Encode[T](in: T): String = {
    Base64.getEncoder.encodeToString(serialize[T](in))
  }

  def base64Decode[T](in: String): T = {
    deserialize[T](Base64.getDecoder.decode(in))
  }
}
