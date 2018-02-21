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
#### KAFKA CLUSTER DESIGN CONCERN
0. How many brokers will we have? this will determine how scalable and fast the 
    cluster will be.
1. How many producers  & consumers will we need inorder to ingest and process encounter,
    obs,orders,person e.t.c?
2. How many partitions will we have per topic? 
    - we will definitely need to come with an intelligent way of calculating number of partition per topic.
    - keeping in mind that this is correlated with "fault tolerance" and speed of access 

4. will we need consumer group in this design 
    - keep in mind that the obs producer will have so many transactions in parallel
5. What Replication factor (RF)? RF is number of copies of each partition stored on different brokers 
    - Keeping in mind replication factor is used to achieve fault tolerance
    - it also depends on number Brokers we will have.  
    - should be predetermined and set during topic creation
    
6. Kafka doesn't retain data forever that's not it's work. There are 2 properties log.retention.ms  and
    log.retention.bytes which determines retention. default is 7 days
    - log.retention.ms - retention by time (default is 7 day) **data will be deleted after 7 days** 
    - log.retention.bytes - retention by size (size is applicable to partition)