

package org.spark.stream

import scala.collection.mutable

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.log4j.Logger

object KafkaProducerFactory {

  import scala.collection.JavaConverters._

  private val Log = Logger.getLogger(getClass)

  private val Producers = mutable.Map[Map[String, Object], KafkaProducer[Array[Byte], Array[Byte]]]()

  def getOrCreateProducer(config: Map[String, Object]): KafkaProducer[Array[Byte], Array[Byte]] = {

    val defaultConfig = Map(
      "key.serializer" -> "org.apache.kafka.common.serialization.ByteArraySerializer",
      "value.serializer" -> "org.apache.kafka.common.serialization.ByteArraySerializer"
    )

    val finalConfig = defaultConfig ++ config

    Producers.getOrElseUpdate(
      finalConfig, {
        Log.info(s"Create Kafka producer , config: $finalConfig")
        val producer = new KafkaProducer[Array[Byte], Array[Byte]](finalConfig.asJava)

        sys.addShutdownHook {
          Log.info(s"Close Kafka producer, config: $finalConfig")
          producer.close()
        }

        producer
      })
  }
}

