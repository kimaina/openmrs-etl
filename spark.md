# Spark Design Architecture
- Spark is a distributted computing platform mostly used in bigdata processing

**Spark Streaming** 
-Is a real-time processing tool that runs on top of the Spark engine.
- enables high-throughput, fault-tolerant stream processing of live data streams. 
- Why spark-streaming

    - Read from Kafka in parallel. In Kafka, a topic can have N partitions, and ideally we’d like to parallelize reading from those N partitions.
    - Write to Kafka from a Spark Streaming application, also, in parallel.
    - http://www.michael-noll.com/blog/2014/10/01/kafka-spark-streaming-integration-example-tutorial/
## Quickstart guide
Download latest Apache Kafka [distribution](http://kafka.apache.org/downloads.html) and un-tar it. 

Start ZooKeeper server:

    ./bin/zookeeper-server-start.sh config/zookeeper.properties

Start Kafka server:

    ./bin/kafka-server-start.sh config/server.properties

Create input topic:

    ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic input

Create output topic:

    ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic output

Start Kafka producer:

    ./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic input

Start Kafka consumer:

    ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic output