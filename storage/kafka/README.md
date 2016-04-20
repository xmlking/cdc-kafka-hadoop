### Install Kafka (one time)
```bash
#  brew install kafka
cd /Developer/Applications/
# wget  http://packages.confluent.io/archive/2.1/confluent-2.1.0-alpha1-2.11.7.zip
http --download  http://packages.confluent.io/archive/2.1/confluent-2.1.0-alpha1-2.11.7.zip
unzip confluent-2.1.0-alpha1-2.11.7.zip
export PATH=$PATH:/Developer/Applications/confluent-2.1.0-alpha1/bin
```

#### working directory
*you will be running all commends below from* **storage/kafka** *directory*

#### To Start Zookeeper
```bash
zookeeper-server-start ./zookeeper.properties
```

#### To Start Kafka
```bash
kafka-server-start ./server.properties
```

#### To Start Schema Registry
schema-registry-start ./schema-registry.properties


#### Create Kafka Topic and partitioning (one time)
```bash
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic maxwell
```

#### List Kafka Topics
```bash
kafka-topics --list --zookeeper localhost:2181
```

#### Display messages on a topic
```bash
kafka-console-consumer --zookeeper localhost:2181 --topic maxwell --from-beginning --property print.key=true
# for kafka-maxwell-connector
kafka-console-consumer --zookeeper localhost:2181 --topic maxwell.test.shop --from-beginning --property print.key=true
# Show Avro data in JSON format in the console.
kafka-avro-console-consumer --zookeeper localhost:2181 --topic maxwell.test.shop --property print.key=true --property schema.registry.url=http://localhost:8081
```


*NOTE: stop Kafka first and then Zookeeper*
