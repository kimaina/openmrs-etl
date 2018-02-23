# Openmrs ETL Streaming topology
![alt text](pics/demo2.png )


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
![alt text](architecture.png)
0. How many brokers will we have? this will determine how scalable and fast the 
    cluster will be.
1. How many producers  & consumers will we need inorder to ingest and process encounter,
    obs,orders,person e.t.c?
2. How many partitions will we have per topic? 
    - we will definitely need to come with an intelligent way of calculating number of partition per topic.
    - keeping in mind that this is correlated with "fault tolerance" and speed of access 
3. Will we allow automatic partition assignment or go manual?
    - going manual is crucial for parallel processing
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
    
7. How many times  should we set the producer to retry after getting an error (default is 0)
8. order of delivery in asynchronous send is not guaranteed? could this be a potential threat
9. Do we need to use consumer group (this can scale up speed of processing)
    - we will have to consider designing for rebalancing using offset
    - why do we even need it ?
        - allows you to parallel process a topic
        - automatically manages partition assignment
        - detects entry/exit/failure of a consumer and perform partition rebalancing
10. What about autocommit? should we override it to false
    - this will allow us to ensure that we don't lose data from the pipline incase our permanent storage service goes down just intime after data processing
11. Schema evolution design strategy
    - so that our producers and consumers can evolve - otherwise we will have to create duplicate producers
     and consume in case of changes in the             