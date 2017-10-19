package scala.xml

import java.io.Serializable
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
}
