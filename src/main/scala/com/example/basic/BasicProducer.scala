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

  // send and forget
  // producer send msg to broker and doesn't care if it was received or not
  // You might lose message be careful
  def send(value: String): Unit = {
    if(sync) sendSync(value) else sendAsync(value)
  }

  // Send a message and wait untill we get a response
  // this method is blocking because it limits you wait
  // to wait for response before doing anything else
  // throughput is minimized because of network delay
  def sendSync(value: String): Unit = {
    val record = new ProducerRecord[String, String](topic, value)
    try {
      producer.send(record).get()
      //  Success: we get a record metadata object (most of the time we dont care abt it
    } catch {
      // Exception: error message - we care because we want to log error message for analysis
      case e: Exception =>
        e.printStackTrace
        System.exit(1)
    }
  }

  // - high throughput bcoz it is non-blocking
  //   - send message and provide callback function to receive ack (record metadata obj)
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
