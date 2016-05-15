debezium-connector-mysql
========================

### Setup

```bash
cd cdc/debezium
curl https://repo1.maven.org/maven2/io/debezium/debezium-connector-mysql/0.1.0/debezium-connector-mysql-0.1.0-plugin.tar.gz | tar xz
```

### Run
```bash
cd cdc/debezium
export KAFKA_HOME=/Developer/Applications/confluent-2.1.0-alpha1
export CLASSPATH=`pwd`/debezium-connector-mysql/*
$KAFKA_HOME/bin/connect-distributed connect-distributed.properties
```


#### add task
```json
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json"localhost:8083/connectors/ -d
{
    "name": "inventory-connector",
    "config": {
        "connector.class": "io.debezium.connector.mysql.MySqlConnector",
        "tasks.max": "1",
        "database.hostname": "localhost",
        "database.port": "3306",
        "database.user": "maxwell",
        "database.password": "XXXXXX",
        "database.server.id": "1",
        "database.server.name": "mysql-server-1",
        "database.binlog": "master.000001",
        "database.whitelist": "test",
        "database.history.kafka.bootstrap.servers": "localhost:9092",
        "database.history.kafka.topic": "schema-changes.inventory"
    }
}
```

#### delete task
```
curl -i -X DELETE localhost:8083/connectors/inventory-connector
```

### Test
```
curl -H "Accept:application/json" localhost:8083/connectors/
curl -i -X GET -H "Accept:application/json" localhost:8083/connectors/inventory-connector


$KAFKA_HOME/bin/kafka-topics --list --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-console-consumer --zookeeper localhost:2181 --topic mysql-server-1.test.book --from-beginning --property print.key=true
$KAFKA_HOME/bin/kafka-console-consumer --zookeeper localhost:2181 --topic mysql-server-1.test.author --from-beginning --property print.key=true
$KAFKA_HOME/bin/kafka-console-consumer --zookeeper localhost:2181 --topic schema-changes.test --from-beginning --property print.key=true
```



