![alt text](https://vladmihalcea.files.wordpress.com/2017/07/debeziumarchitecture1.png?w=788&resize=788%2C390)

# Debezium
- is a distributed platform that turns your existing databases into event streams, 
so applications can see and respond immediately to each row-level change in the databases.

## Getting started
- Running Debezium involves three major services: Zookeeper, Kafka, and Debezium’s connector service. 
- Running multiple services locally can be confusing, so we’re going to
 use a separate terminal to run each container
 
 
 Start ZooKeeper container:
 
      docker run -it --rm --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 debezium/zookeeper:0.7
 
 Start Kafka container:
 
      docker run -it --rm --name kafka -p 9092:9092 --link zookeeper:zookeeper debezium/kafka:0.7
 
 Start Mysql container:
 
     docker run -it --rm --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=openmrs -e MYSQL_USER=debezium -e MYSQL_PASSWORD=dbz debezium/example-mysql:0.7
 
 Start Openmrs container:
  
      docker run -it --rm --name openmrs --link mysql:mysql -p 8080:8080 -e DB_DATABASE=openmrs -e DB_HOST=0.0.0.0 -e DB_USERNAME=mysqluser -e DB_PASSWORD=mysqlpw openmrs/openmrs-reference-application-distro:latest
      
 
 Start Mysql Client:

     docker run -it --rm --name mysqlterm --link mysql --rm mysql:5.7 sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD"'
 
 Start Kafka Connect:
 
     docker run -it --rm --name connect -p 8083:8083 -e GROUP_ID=1 -e CONFIG_STORAGE_TOPIC=my_connect_configs -e OFFSET_STORAGE_TOPIC=my_connect_offsets --link zookeeper:zookeeper --link kafka:kafka --link mysql:mysql debezium/connect:0.7
 
 Fire Up Kafka Connect:
 
    ```shell
    curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '{ "name": "openmrs-connector", "config": { "connector.class": "io.debezium.connector.mysql.MySqlConnector", "tasks.max": "1", "database.hostname": "mysql", "database.port": "3306", "database.user": "debezium", "database.password": "dbz", "database.server.id": "184054", "database.server.name": "dbserver1", "database.whitelist": "openmrs", "database.history.kafka.bootstrap.servers": "kafka:9092", "database.history.kafka.topic": "dbhistory.openmrs" } }'
    ```

 Verify Openmrs connector is up:
 
     curl -H "Accept:application/json" localhost:8083/connectors/
     curl -i -X GET -H "Accept:application/json" localhost:8083/connectors/openmrs-connector
 
 Watch Topic:
     
     docker run -it --name watcher --rm --link zookeeper:zookeeper debezium/kafka:0.7 watch-topic -a -k dbserver1.openmrs.person    