

package org.spark.stream

import kafka.serializer.DefaultDecoder
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils

class KafkaDStreamSource(config: Map[String, String]) {

  def createSource(ssc: StreamingContext, topic: String): DStream[KafkaPayload] = {
    val kafkaParams = config
    val kafkaTopics = Set(topic)

    KafkaUtils.
      createDirectStream[Array[Byte], Array[Byte], DefaultDecoder, DefaultDecoder](
      ssc,
      kafkaParams,
      kafkaTopics).
      map(dstream => KafkaPayload(Option(dstream._1), dstream._2))
  }

}

object KafkaDStreamSource {
  def apply(config: Map[String, String]): KafkaDStreamSource = new KafkaDStreamSource(config)
}
