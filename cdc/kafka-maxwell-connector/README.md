Kafka Maxwell Connector
=======================

kafka-maxwell-connector is a plugin that allows you to easily replicate MySQL changes to Apache Kafka. It uses the fantastic [Maxwell](https://github.com/zendesk/maxwell) project to read MySQL binary logs in near-real time. It runs as a plugin within the [Kafka Connect](http://kafka.apache.org/documentation.html#connect) framework, which provides a standard way to ingest data into Kafka.

By using Maxwell, we are able to:
- replicate data from an unpatched MySQL server
- Parse ALTER/CREATE/DROP table statements, which allows us to always have a correct view of the MySQL schema

By plugging in to the Kafka Connect framework, we are able to:
- benefit from standardized best practices for Kafka producers and consumers
- run in distributed or standalone mode
- manage the Kafka MySQL Connector via REST interfaces
- manage offsets in Kafka

Status
------
This code is a work-in-progress.

What's done:
* Offsets stored in Kafka by the Kafka Connect framework
* Simple schema extraction for row changes. This allows one to supply Converters such as confluents avro converter to control serialization of the messages.
* Each table is written to its own topic. The Kafka primary key is the row's primary key.
* It supports primary keys which are ints.

What needs to be done:
* Support primary keys of any SQL type
* Testing.
* Packaging
* Logging


### Build

1.  Pull down the Maxwell fork.
    ```
    (with in separate directory outside this project: e.g., /work/)
    git clone https://github.com/xmlking/maxwell.git maxwell
    ```

2.  Build my Maxwell fork
    ```
    (cd maxwell && git checkout 1.1.0-kafka-connect && mvn install)
    ```

3.  Build and "install" connector within the build directory.
    ```
    (with in root project directory i.e., cdc-kafka-hadoop)
    gradle :cdc/kafka-maxwell-connector:build :cdc/kafka-maxwell-connector:installDist -x test
    ```

### Run

Start MySQL, ZooKeeper, Kafka, Schema Registry as [described](/storage/kafka/) and then run `kafka-maxwell-connector`

    ```
    (with in cdc/kafka-maxwell-connector directory)
    export CLASSPATH=`pwd`/build/install/cdc/kafka-maxwell-connector/kafka-maxwell-connector-0.1.0-SNAPSHOT.jar:`pwd`/build/install/cdc/kafka-maxwell-connector/lib/*
    export PATH=$PATH:/Developer/Applications/confluent-2.1.0-alpha1/bin
    connect-standalone copycat-standalone.properties connect-mysql-source.properties
    ```

### Test

    ```
    mysql> INSERT INTO test.shop (version, name, owner, phone_number) values (0, 'aaaa', 'bbbb', '1111111111');
    ```

You can also generate database transactions using [testApp](/testApp/).

####  Display messages on a topic

    ```bash
    export PATH=$PATH:/Developer/Applications/confluent-2.1.0-alpha1/bin
    # Show  data in JSON format in the console.
    kafka-console-consumer --zookeeper localhost:2181 --topic maxwell.test.shop --from-beginning --property print.key=true
    # Show  data in Avro format in the console.
    kafka-avro-console-consumer --zookeeper localhost:2181 --topic maxwell.test.shop --property print.key=true --property schema.registry.url=http://localhost:8081
    ```

    ```json
    {"id":{"long":7},"name":{"string":"aaaa"}}      {"data":{"id":{"long":7},"version":{"long":0},"name":{"string":"aaaa"},"owner":{"string":"bbbb"},"phone_number":{"string":"1111111111"}},"old":{"id":null,"version":null,"name":null,"owner":null,"phone_number":null},"database":"test","table":"shop","mut_type":"insert","xid":12802,"ts":1461111727}
    ```
