package com.example.basic

object DemoBasicProducer {
  def main(args: Array[String]): Unit = {

    if (args.length != 5) {
      println("DemoBasicProducer {broker-list} {topic} {type sync/async} {count} {delay}")
      return;
    }

    val brokerList = args(0)

    val topic = args(1)

    val sync = args(2).toLowerCase match {
      case "sync" => true
      case _ => false
    }

    val count = args(3).toInt
    val delay = args(4).toInt

    val myProducer = BasicProducer(topic, brokerList, sync)

    println("Starting....")
    val startTime = System.currentTimeMillis()

    /* send some numbers to kafka */
    for (i <- 1 to count)  {
      myProducer.send(i.toString);
      Thread.sleep(delay);
    }
    val endTime = System.currentTimeMillis()
    println(s"Done.  It took ${endTime-startTime}ms")

    myProducer.close();
  }
}
