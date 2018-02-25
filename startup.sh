#!/bin/bash



# Start Kafka producer:
 docker-compose -f docker-compose.yaml exec kafka /kafka/bin/kafka-console-producer.sh \
     --broker-list kafka:9092 \
     --topic input



# Start Kafka consumer:
 docker-compose -f docker-compose.yaml exec kafka /kafka/bin/kafka-consumer.sh \
     --bootstrap-server kafka:9092 \
     --topic output