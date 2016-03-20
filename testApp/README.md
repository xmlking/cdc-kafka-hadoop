testApp
=======

test app to generate load on MySQL


#### grant database permissions for grails(one time)
```sql
mysql -u root -p
mysql> CREATE DATABASE test;
mysql> GRANT ALL on test.* to 'grails'@'%' identified by 'grails';
```

#### run app
```bash
# from cdc-kafka-hadoop directory
gradle -a :testApp:bootRun
```

#### access
http://localhost:8080/


### add two primary key for Shop table.
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

logger("org.hibernate.SQL", DEBUG, ["STDOUT"], false)
logger("org.hibernate.type.descriptor.sql.BasicBinder", TRACE, ["STDOUT"], false)
