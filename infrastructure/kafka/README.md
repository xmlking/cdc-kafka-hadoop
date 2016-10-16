### Install Kafka (one time)

> We need *Kafka Streams Tech Preview* for this demo. Lets manually install Kafka until final version is available on homebrew

```bash
# brew install kafka
cd /Developer/Applications/
curl http://packages.confluent.io/archive/3.0/confluent-3.0.1-2.11.tar.gz | tar xz
```

#### Start Kafka Services

*you will be running all commends below from* **infrastructure/kafka** *directory*

```
cd infrastructure/kafka
export KAFKA_HOME=/Developer/Applications/confluent-3.0.1
```

#### To Start Zookeeper
```bash
$KAFKA_HOME/bin/zookeeper-server-start ./zookeeper.properties
```

#### To Start Kafka
```bash
$KAFKA_HOME/bin/kafka-server-start ./server.properties
```

#### To Start Schema Registry
```bash
$KAFKA_HOME/bin/schema-registry-start ./schema-registry.properties
```

#### Create Kafka Topic and partitioning (one time)
```bash
$KAFKA_HOME/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic maxwell
```

#### List Kafka Topics
```bash
$KAFKA_HOME/bin/kafka-topics --list --zookeeper localhost:2181
```

#### Display messages on a topic
```bash
$KAFKA_HOME/bin/kafka-console-consumer --zookeeper localhost:2181 --topic maxwell --from-beginning --property print.key=true
# for kafka-maxwell-connector
$KAFKA_HOME/bin/kafka-console-consumer --zookeeper localhost:2181 --topic maxwell.test.shop --from-beginning --property print.key=true
# Show Avro data in JSON format in the console.
$KAFKA_HOME/bin/kafka-avro-console-consumer --zookeeper localhost:2181 --topic maxwell.test.shop --property print.key=true --property schema.registry.url=http://localhost:8081
```


*NOTE: stop Kafka first and then Zookeeper*
