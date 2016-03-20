testApp
=======

test app to generate load on MySQL


#### Grant database permissions for grails(one time)
```sql
mysql -u root -p
mysql> CREATE DATABASE test;
mysql> GRANT ALL on test.* to 'grails'@'%' identified by 'grails';
```

#### Run App
```bash
# from cdc-kafka-hadoop directory
gradle -a :testApp:bootRun
```

#### Access
http://localhost:8080/


### Add two primary keys for Shop table.
> Maxwell should produce both primary keys in the Kafka key.
> {"database":"test","table":"shop","pk.id":4,"pk.name":"aaa"}

```sql
DROP TABLE test.shop;
CREATE TABLE shop
(
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  version BIGINT(20) NOT NULL,
  name VARCHAR(255) NOT NULL,
  owner VARCHAR(255) NOT NULL,
  phone_number VARCHAR(255) NOT NULL,
  primary key (id, name)
);
```
