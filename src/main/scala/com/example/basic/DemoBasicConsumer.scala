package com.example.basic

import org.apache.kafka.clients.consumer.ConsumerRecords

object DemoBasicConsumer {

  def main(args: Array[String]): Unit = {

    if (args.length != 4) {
      println("DemoBasicConsumer {zooKeeper} {groupId} {topic} {waitTime}")
      return;
    }

    val zooKeeper = args(0)
    val groupId = args(1)
    val topic = args(2)
    val waitTime = args(3)

    val myConsumer = BasicConsumer(zooKeeper, groupId, waitTime)
    myConsumer.subscribe(topic)


    while(true)
      {
        println("working")
        val message = myConsumer.read(topic)
        if(message.length < 1)
          {
            myConsumer.shutdown()
            println("bye")
          }

        println(message)
      }
  }

}
