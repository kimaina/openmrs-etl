# Openmrs ETL Streaming topology

![alt text](architecture.png)

This demo automatically deploys the topology of services as defined in [Debezium Tutorial](http://debezium.io/docs/tutorial/) document.

## Using MySQL

```shell
# Start the topology as defined in http://debezium.io/docs/tutorial/
export DEBEZIUM_VERSION=0.7
docker-compose -f docker-compose-mysql.yaml up

# Start MySQL connector
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-mysql.json

# Consume messages from a Debezium topic
docker-compose -f docker-compose-mysql.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --from-beginning \
    --property print.key=true \
    --topic dbserver1.openmrs.obs

# Modify records in the database via MySQL client
docker-compose -f docker-compose-mysql.yaml exec mysql bash -c 'mysql -u $MYSQL_USER -p$MYSQL_PASSWORD openmrs'

# Shut down the cluster
docker-compose -f docker-compose-mysql.yaml down

# Portainer is a free open-source web application that runs as a container itself. You can install and start it with:
docker run -d -p 9000:9000 -v /var/run/docker.sock:/var/run/docker.sock portainer/portainer

# building scala sing sbt
sbt package
sbt run 
 
```

#### Necessary concepts
- kafka is a distributed streaming platform which can be used 
  for stream processing and a connector to import and export 
  bulk data from databases and other systems
   
* **producer** is an application that sends messages to kafka (publisher)
* **message** is a small to medium sized piece of data (key-value pair)
                -programmatically you need to convert to bytes (kafka only accepts bytes)
* **consumer** is an app that receives data from kafka (subscriber)
* **broker** is a single kafka server 
* **cluster** group of computers
* **Topic** a unique name for a kafka(data) stream
                - push message to a particular topic
                - 
* **partitions** part of a topic - a topic can be really huge (100gb+) 
                 a topic can be larger than a single computer (broker)
                 in that case the broker might have challenge in storing 
                 the whole topic. the obvious solution is to distribute parts of
                 the topic in multiple brokers as partitions.
                 - every partition sits on a single machine (you cant break it further)
                 - every partition has a unique index id (per topic)
                 
* **Offset** uniq id for a message within a partition
                - this index is assigned as message arrives (in order of arrival)
                - this id is immutable
                - it is a global unique identifier of a message
                - for you to access a particular message you need (topicName -> partitionNum -> offset)
* **consumer groups** a group of consumers acting as a single logic unit (scalability)
                - many consumer form a group to share work 
                - think of it as groupWork where each member works
                  on a unit of the larger task (divide and conquer) 
                - when datastream becomes too huge for a consumer to handle, 
                  you need to divide the task (partitions) to multipe consumers called consumer group
                - this is highly useful to increase speed of processing  and scalability
                - this is a tool for scalability
                - note that max number of consumers in a group is equal to total number of 
                  partition in a topic
                - kafka doesnt allow more than 2 consumers to read on the same partition simultaneously
                    (inroder to avoid double reading of records)  



                  
#### TODO
0. The first question how many brokers will we have? this will determine how scalable and fast the 
    cluster will be.
1. How many producers  & consumers will we need inorder to ingest and process encounter,
    obs,orders,person e.t.c?
2. How many partitions will we have per topic? (1 leader and n followers).
    - we will definately need to come with an intelligent way of calculating number of partition per topic.
    - keeping in mind that this is correlated with "fault tolerance" and speed of access 

4. will we need consumer group in this design (keep in mind that the obs producer will have so many transctions in parallel)  
5. 

## How it works
- the following are cmds for starting up a kafka-debzium cluster using shell script (this can be docarized)
- this project has docarized most of this steps
- other steps(logic) have been coded using scala
- data transformation is handled by spark
- Note that these steps are here only for demo on how the topology works. consumer api and 
   producer api have been used in this project using scala

#### Step 0: install zookeeper
#### Step 1: start zookeeper
* **zookeeper** - coordinate brokers and maintains their config inorder to avoid race conditions
                - necessary for creating a cluster.
                - kafka is shipped with zookeeper
                - cmd: bin/zookeeper-server-start.sh config/zookeeper.properties  
                - config/zookeeper.properties  is the default config file
                - default port is 2181
                - THIS MUST BE STARTED BEFORE KAFKA IS STARTED
#### Step 2: Install kafka
* **kafka broker** - server
                - need a config file
                - cmd: bin/kafka-server-start.sh config/server.properties  
                - config/server.properties  is the default config file
                - each broker must have individual config file with unique props:
                    - broker.id
                    - port number
                    - log.dirs
                - default port is 9092
                - kafka creates topic by default automatically if you send message without specifying
                
* **kafka topic** -
                - cmd: bin/kafka-server-topic.sh 
                    --zoookeeper localhost:2181 
                    --create --topic abctopic --partition 2 
                    --repication-factor 1
                    
                 - repication-factor - # of copies of each partition stored on different brokers
                 - how to get full details of a topic
                    - cmd:  bin/kafka-server-topic.sh 
                                --zoookeeper localhost:2181 
                                --describe --topic abctopic
                                
                 - ISR means In sync replica 
                        - you can have multiple replicas but not all are insync with the leader 
                        - isr shows the number of replicas that are insync with the leader
 
* **kafka producer** - cmd: bin/kafka-console-producer.sh --broker-list localhost:9092 
                            --topic abctopic
                     - programmatically you need:
                        1. to create  KafkaProducerObject (Producer <String>)
                                - 3 props are mandatory
                                        - bootstrap.servers. (provide multiple)
                                        - key.serializer. 
                                        - value.serializers
                                        
                        2. then create (ProducerRecord <String>) (here u specify topic and message)
                            
* **kafka consumer** - cmd: bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 
                            --topic abctopic
                      - same command as producer (broker-list is same as bootstrap-sever)      
                      
#### Step 3: Configure for fault tolerance

- fault tolerance is important because it makes sure cluster continues to operate in the event that
    one broker fails
- fault tolerance can be done by making multiple copies of your partitions on different brokers

* **Replication factor**  --replication-factor 3
    - replication factor is used to achieve fault tolerance
    - 3 means 3 replica will be created
    - use this fro super sensitive data
    - use this if you have a lot of downtimes
    - use this if your server is not powerful with lots of power outage
    - rf is defined at the topic level and is applied to all partions within that topic
    
* **Leader and Follower Model**     
- Kafka implements this model
- For every partition one broker is elected as the leader
- for every partition we have a leader. the leader handles all communication
- The leader takes care of all client interractions
- The leader has the responsibility to receive and send messages from specific partition
- Producer and consumers can only interract with the leader
- the leader relays all copies to the followers


#### Broker Configuration 
- important for usecase customization

1. zookeeper.connect - zookeeper address - links multiple brokers to form a cluster
                     - necessary to form a  cluster
                     - all brokers are running on diff server- how do they know abt each other?
                     - If they don't know about each other the are not part of the cluster
                     - It is critical for all brokers to know the zookeeper address
2. delete.topic.enable - default value is false
                       - in production you should always set to false otherwise set it to true
                       - in development mode you might want to delete a topic
3. auto.create.topics.enable
                  - default is true
                  - kafka automatically creates  a topic if you send data without a topic
                  - in prod you should set it to false coz you need a more controlled approach
                  - setting it to true will make kafka reject any incoming messages without topic
4. default.replication.factor
5. num.partitions
        - default for both of them is 1
        
6. log.retention.ms - retention by time (default is 7 day) **data will be deleted after 7 days** 
7. log.retention.bytes - retention by size (size is applicable to partition)
         - kafka doesn't retain data forever that's not it's work
         - REMEMBER KAFKA IS NOT A DATABASE where you can store data and query it later
         - kafka is only a message broker after delivering data it cleans itself
       
                        
