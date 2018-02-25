

package org.spark.stream

import scala.reflect.ClassTag
import scala.util.Failure
import scala.util.Success

import com.twitter.bijection.Injection
import com.twitter.bijection.avro.SpecificAvroCodecs
import org.apache.avro.specific.SpecificRecordBase
import org.apache.log4j.Logger

class KafkaPayloadAvroSpecificCodec[A <: SpecificRecordBase : ClassTag] extends Serializable {

  @transient lazy private val logger = Logger.getLogger(getClass)
  @transient lazy implicit private val avroSpecificInjection = SpecificAvroCodecs.toBinary[A]

  def decodeValue(payload: KafkaPayload): Option[A] = {
    val decodedTry = Injection.invert[A, Array[Byte]](payload.value)
    decodedTry match {
      case Success(record) =>
        Some(record)
      case Failure(ex) =>
        logger.warn("Could not decode payload", ex)
        None
    }
  }

  def encodeValue(value: A): KafkaPayload = {
    val encoded = Injection[A, Array[Byte]](value)
    KafkaPayload(None, encoded)
  }

}

object KafkaPayloadAvroSpecificCodec {
  def apply[A <: SpecificRecordBase : ClassTag](): KafkaPayloadAvroSpecificCodec[A] =
    new KafkaPayloadAvroSpecificCodec[A]
}
