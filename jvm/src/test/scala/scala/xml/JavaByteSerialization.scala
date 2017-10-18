package scala.xml

import java.io.Serializable
import java.util.Base64
import org.apache.commons.lang3.SerializationUtils

object JavaByteSerialization {
  def roundTrip[T <: Serializable](obj: T): T = {
    SerializationUtils.roundtrip(obj)
  }

  def serialize[T <: Serializable](in: T): Array[Byte] = {
    SerializationUtils.serialize(in)
  }

  def deserialize[T <: Serializable](in: Array[Byte]): T = {
    SerializationUtils.deserialize(in)
  }

  def base64Encode[T <: Serializable](in: T): String = {
    Base64.getEncoder.encodeToString(serialize[T](in))
  }

  def base64Decode[T <: Serializable](in: String): T = {
    deserialize[T](Base64.getDecoder.decode(in))
  }
}
