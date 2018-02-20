package com.example.basic

import java.util.Properties
import org.apache.kafka.clients.producer.{Callback, RecordMetadata, ProducerRecord, KafkaProducer}

import scala.concurrent.Promise


case class BasicProducer(topic: String, brokerList:String, sync: Boolean) {
  val kafkaProps = new Properties()
  kafkaProps.put("bootstrap.servers", brokerList)

  // This is mandatory, even though we don't send keys
  kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  kafkaProps.put("acks", "1")
  kafkaProps.put("producer.type", if(sync) "sync" else "async")

  // how many times to retry when produce request fails?
  kafkaProps.put("retries", "3")
  kafkaProps.put("linger.ms", "5")

  //this is our actual connection to Kafka!
  private val producer = new KafkaProducer[String, String](kafkaProps)

  def send(value: String): Unit = {
    if(sync) sendSync(value) else sendAsync(value)
  }

  def sendSync(value: String): Unit = {
    val record = new ProducerRecord[String, String](topic, value)
    try {
      producer.send(record).get()
    } catch {
      case e: Exception =>
        e.printStackTrace
        System.exit(1)
    }
  }

  def sendAsync(value: String):Unit = {
    val record = new ProducerRecord[String, String](topic, value)
    val p = Promise[(RecordMetadata, Exception)]()
    producer.send(record, new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        p.success((metadata, exception))
      }
    })

  }

  def close():Unit = producer.close()
}
